package com.example.shivamgandhi.gesture;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
    String pName;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);

        /**
         * show progress dialog
         */
        showProgressDialog();

        initializeAll();
        pName = mVars.getPlayerName();
        mGameDatabase.updateUserStatus("online");
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
         * START GAME IN DEFAULT TIME[90 SEC]
         */
        new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                cntdwnTxt.setText("starting in:- " + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));

                if (cntdwnTxt.getText().toString().equals("starting in:- 00:00:15")) {
                    readyBtn.setEnabled(false);
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
                    cntdwnTxt.setVisibility(View.INVISIBLE);
                    mDatabaseReference.child("game").child(mVars.getGameID()).removeValue();
                    Intent i = new Intent(WaitingScreen.this, MainActivity.class);
                    startActivity(i);

                }
            }
        }.start();

        handler = new Handler();
        handler.postDelayed(runnable, 10000);

    }// end of onCreate()

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

           Log.d("handler","calling in every 10 sec");
           checkInRange();

           handler.postDelayed(this, 10000);
        }
    };

    private void showProgressDialog() {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.show();
        progress.setCancelable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress.dismiss();
            }
        }, 5000);

    }

    private void initializeAll() {

        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        gameIDTv = findViewById(R.id.waiting_gID);

        Log.d("waiting/pName", "output");
        Log.d("waiting/pName", mVars.getPlayerName());
        gameIDTv.setText("GameId:- " + mVars.getGameID());
        cntdwnTxt = findViewById(R.id.waiting_countDown);
        cntdwnTxt1 = findViewById(R.id.waiting_countDown1);
        readyBtn = findViewById(R.id.waiting_btnReady);
        readyBtn.setText("ready");
        cancelBtn = findViewById(R.id.waiting_btnCancel);
        playersListView = findViewById(R.id.waiting_listView);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.waiting_btnCancel:

                mVars.setPlayerName(pName);
                mGameDatabase.removePlayerFromGame();

                Intent intent = new Intent(WaitingScreen.this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.waiting_btnReady:

                /**
                 * if player is "READY"
                 */
                if (readyBtn.getText().toString().equals("ready")) {
                    mGameDatabase.updateReadyValue("ready");
                    readyBtn.setText("not ready");


                } else {
                    /**
                     * if user is "NOT READY"
                     */
                    mGameDatabase.updateReadyValue("not ready");
                    readyBtn.setText("ready");

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

            Player player = dataSnapshot.getValue(Player.class);
            Log.d("waiting/dataChng", player.name);
            Log.d("waiting/dataChng", player.ready);
            String name = player.name;
            String ready = player.ready;
            for (int i = 0; i < playerName.size(); i++) {
                if (name.equals(playerName.get(i))) {
                    playStatus.set(i, ready);
                    mCustomAdapter.notifyDataSetChanged();
                }
            }
            if (checkAllReady(playStatus)) {
                startTimerForTen();
            } else {
                mGameDatabase.changeGameStatus("waiting");
                cntdwnTxt1.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            Player player = dataSnapshot.getValue(Player.class);
            String pName = player.name;

            for (int i = 0; i < playerName.size(); i++) {
                if (pName.equals(playerName.get(i))) {
                    playerName.remove(i);
                    playStatus.remove(i);
                    mCustomAdapter.notifyDataSetChanged();
                }
            }

            Toast.makeText(WaitingScreen.this, "Some One Left game", Toast.LENGTH_SHORT).show();


        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    private void startTimerForTen() {

        cntdwnTxt.setVisibility(View.INVISIBLE);
        cntdwnTxt1.setVisibility(View.VISIBLE);
        mGameDatabase.changeGameStatus("All Ready");
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

                Log.d("inside Finish","true");

                mDatabaseReference.child("game").child(mVars.getGameID()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String stat = dataSnapshot.getValue().toString();
                        Log.d("inside Finish",stat);
                        if (stat.equals("all ready")) {
                            Log.d("inside Finish","change status");
                            mDatabaseReference.child("game").child(mVars.getGameID()).child("status").setValue("play");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }.start();

    }

    private boolean checkAllReady(ArrayList<String> pStatus) {
        boolean b = true;
        ArrayList<String> status = pStatus;

        for (int p = 0; p < status.size(); p++) {
            if (status.get(p).equals("ready")) {

            } else {
                b = false;
            }
        }
        return b;
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

    private void checkInRange() {

        mDatabaseReference.child("game").child(mVars.getGameID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double lat = (Double) dataSnapshot.child("latitude").getValue();
                Double log = (Double) dataSnapshot.child("longitude").getValue();


                LatLng latLngA = new LatLng(lat, log);
                LatLng latLngB = new LatLng(mVars.getLat(), mVars.getLog());

                Location locationA = new Location(LocationManager.GPS_PROVIDER);
                locationA.setLatitude(latLngA.latitude);
                locationA.setLongitude(latLngA.longitude);
                Location locationB = new Location(LocationManager.GPS_PROVIDER);
                locationB.setLatitude(latLngB.latitude);
                locationB.setLongitude(latLngB.longitude);

                double distance = locationA.distanceTo(locationB);
                Log.d("waitingScreen/distnace", "FINAL DISTANCE:- " + distance);

                if (distance > 500) {
                    Toast.makeText(WaitingScreen.this, "Game is not in range", Toast.LENGTH_SHORT).show();
                    mGameDatabase.removePlayerFromGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {

    }
}
