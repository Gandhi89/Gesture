package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaitingScreen extends AppCompatActivity implements View.OnClickListener {


    GameDatabase mGameDatabase;
    Vars mVars;
    TextView gameIDTv, cntdwnTxt, cntdwnTxt1;
    Button readyBtn, cancelBtn;
    boolean b = true;
    boolean q = false;
    ListView playersListView;
    ArrayList<String> playerName = new ArrayList<>();
    ArrayList<String> playStatus = new ArrayList<>();
    CustomAdapter mCustomAdapter;
    private static final String FORMAT = "%02d:%02d:%02d";
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);

        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        gameIDTv = findViewById(R.id.waiting_gID);
        cntdwnTxt = findViewById(R.id.waiting_countDown);
        cntdwnTxt1 = findViewById(R.id.waiting_countDown1);
        readyBtn = findViewById(R.id.waiting_btnReady);
        readyBtn.setText("ready");
        cancelBtn = findViewById(R.id.waiting_btnCancel);
        playersListView = findViewById(R.id.waiting_listView);

        mCustomAdapter = new CustomAdapter(WaitingScreen.this, playerName, playStatus);
        playersListView.setAdapter(mCustomAdapter);

        readyBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        /**
         * if new player added
         */
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addChildEventListener(new playerAddedListner());
        /**
         * if status of game changed
         */
        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").addValueEventListener(new watchStatus());


        /**
         * START GAME IN DEFAULT TIME[30 SEC]
         */

        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                cntdwnTxt.setText("starting in:- " + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

                if (cntdwnTxt.getText().toString().equals("starting in:- 00:00:10")) {
                    /**
                     * if "all ready" timer is visible , dont show default timer
                     */
                    if (cntdwnTxt1.getVisibility() == View.VISIBLE) {

                    } else {
                        cntdwnTxt.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFinish() {
                /**
                 * minimum two players are in game?
                 */
                if (playerName.size() > 1) {
                    mDatabaseReference.child("game").child(mVars.getGameID()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String stats = dataSnapshot.getValue().toString();
                            if (stats.equals("play")) {

                            } else {
                                /**
                                 * start game
                                 */
                                mGameDatabase.changeGameStatus("play");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    /**
                     * since NO PLAYER joined!, delete game
                     */
                    Toast.makeText(WaitingScreen.this, "No player Joined ! Try creating game Again ", Toast.LENGTH_SHORT).show();
                    cntdwnTxt.setVisibility(View.INVISIBLE);
                    mDatabaseReference.child("game").child(mVars.getGameID()).removeValue();
                    Intent i = new Intent(WaitingScreen.this, MainActivity.class);
                    startActivity(i);

                }
            }
        }.start();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.waiting_btnReady:
                /**
                 * if player is "READY"
                 */
                if (readyBtn.getText().toString().equals("ready")) {
                    mGameDatabase.updateReadyValue("ready");
                    readyBtn.setText("not ready");
                    new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long l) {
                        }

                        @Override
                        public void onFinish() {
                            readData(new MyCallback() {
                                @Override
                                public void onCallback(ArrayList<String> pName, ArrayList<String> pStat) {
                                    playerName = pName;
                                    playStatus = pStat;

                                    if (playStatus.size() > 1) {
                                        String getStat = playStatus.get(0);

                                        for (int u = 0; u < playStatus.size(); u++) {
                                            if (getStat.equals(playStatus.get(u))) {
                                            } else {
                                                b = false;
                                            }
                                        }

                                    }
                                    mCustomAdapter = new CustomAdapter(WaitingScreen.this, playerName, playStatus);
                                    playersListView.setAdapter(mCustomAdapter);
                                }
                            });
                            /**
                             * if every player is ready, start game in 10 sec
                             */
                            if (b) {
                                mGameDatabase.changeGameStatus("all ready");
                                allAreReady();
                            } else {
                                cntdwnTxt1.setVisibility(View.INVISIBLE);
                            }
                        }
                    }.start();

                } else {
                    /**
                     * if user is "NOT READY"
                     */
                    mGameDatabase.updateReadyValue("not ready");
                    readyBtn.setText("ready");
                    mGameDatabase.changeGameStatus("waiting");
                    new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long l) {
                        }

                        @Override
                        public void onFinish() {
                            readData(new MyCallback() {
                                @Override
                                public void onCallback(ArrayList<String> pName, ArrayList<String> pStat) {
                                    playerName = pName;
                                    playStatus = pStat;

                                    mCustomAdapter = new CustomAdapter(WaitingScreen.this, playerName, playStatus);
                                    playersListView.setAdapter(mCustomAdapter);
                                }
                            });
                        }
                    }.start();
                }

                break;
        }
    }

    /**
     * listen for player to be added in particular game
     */
    private class playerAddedListner implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            Player player = dataSnapshot.getValue(Player.class);
            playerName.add(player.name);
            playStatus.add(player.ready);
            mCustomAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    /**
     * listen for status change
     */
    private class watchStatus implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                if (dataSnapshot.getValue() != null) {
                    try {
                        if (dataSnapshot.getValue().equals("play")) {
                            Intent i = new Intent(WaitingScreen.this, HomeActivity.class);
                            startActivity(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("TAG", " it's null.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    public interface MyCallback {
        void onCallback(ArrayList<String> playName, ArrayList<String> playStat);
    }

    public void readData(final MyCallback myCallback) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        final DatabaseReference ref1;
        final ArrayList<String> names = new ArrayList<>();
        final ArrayList<String> stats = new ArrayList<>();

        ref1 = mDatabaseReference.child("game").child(mVars.getGameID()).child("players");
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Player user = snapshot.getValue(Player.class);

                    names.add(user.name);
                    stats.add(user.ready);
                    myCallback.onCallback(names, stats);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * method to calculate all players are ready or not
     */
    private void allAreReady() {
        cntdwnTxt.setVisibility(View.INVISIBLE);
        cntdwnTxt1.setVisibility(View.VISIBLE);
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                cntdwnTxt1.setText("starting in:- " + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            @Override
            public void onFinish() {
                readData(new MyCallback() {
                    @Override
                    public void onCallback(ArrayList<String> pName, ArrayList<String> pStat) {
                        playerName = pName;
                        playStatus = pStat;

                        if (playStatus.size() > 1) {

                            mDatabaseReference.child("game").child(mVars.getGameID()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String sta = dataSnapshot.getValue().toString();
                                    if (sta.equals("all ready")) {
                                        q = true;
                                    } else {
                                        q = false;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                });
                if (q) {
                    mGameDatabase.changeGameStatus("play");
                } else {
                    cntdwnTxt1.setVisibility(View.INVISIBLE);
                    cntdwnTxt.setVisibility(View.VISIBLE);
                    mGameDatabase.changeGameStatus("waiting");
                }
            }
        }.start();
    }
}
