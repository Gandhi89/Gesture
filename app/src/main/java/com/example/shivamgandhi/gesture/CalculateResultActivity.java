package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CalculateResultActivity extends AppCompatActivity implements View.OnClickListener {

    TextView displayResultTv, countDownTimerTv;
    Button quitGameBtn;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    String stat;
    String gStatus;

    ArrayList<String> userChoice = new ArrayList<>();
    ArrayList<String> winingIDs = new ArrayList<>();
    int count_r = 0, count_p = 0, count_s = 0, count_none = 0;
    String getWiningStatus_r, getWiningStatus_p, getWiningStatus_s;
    ArrayList<String> winingStatus = new ArrayList<>();
    private static final String FORMAT = "%02d:%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_result);
        displayResultTv = findViewById(R.id.calculateResultActivity_textview);
        countDownTimerTv = findViewById(R.id.calculateResultActivity_cnt);
        quitGameBtn = findViewById(R.id.calculateResultActivity_quitGame);
        quitGameBtn.setOnClickListener(this);

        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mGameDatabase = new GameDatabase();

        readData(new MyCallback() {
            @Override
            public void onCallback(String value) {
                gStatus = value;
                Log.d("cal/gameStatus:-", "inside readData(interface):- " + gStatus);
            }
        });

        /**
         * get RPS of all players
         */
        /**
         * TODO :- what if child is added but that player does not respond ?????
         */
        userChoice = mGameDatabase.getRPSValue();
        /**
         *  wait for 7 sec, SYNC purpose
         */
        new CountDownTimer(7000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Toast.makeText(CalculateResultActivity.this, userChoice.toString(), Toast.LENGTH_SHORT).show();
                Log.d("CalculateResult/array:-", userChoice.toString());
                mGameDatabase.setCount_rps(userChoice);
                count_r = mGameDatabase.getCount_r();
                count_p = mGameDatabase.getCount_p();
                count_s = mGameDatabase.getCount_s();
                count_none = mGameDatabase.getCount_n();
                winingStatus = mGameDatabase.calResult(count_r, count_p, count_s, count_none);
                Toast.makeText(CalculateResultActivity.this, winingStatus.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Calcula/winingStatus:-", winingStatus.toString());
                getWiningStatus_r = winingStatus.get(0);
                getWiningStatus_p = winingStatus.get(1);
                getWiningStatus_s = winingStatus.get(2);

                mGameDatabase.updatePlayerStatus(getWiningStatus_r, getWiningStatus_p, getWiningStatus_s);
                counter();

            }
        }.start();
    }

    /**
     * fetch status of player and display in textView
     */
    private void counter() {
        new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        /**
                         * get number of players in game
                         */
                        long count = dataSnapshot.getChildrenCount();
                        Log.d("val/No_players:-", dataSnapshot.getChildrenCount() + "");
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                            String key = postSnapshot.getKey();
                            Player player = postSnapshot.getValue(Player.class);
                            stat = player.status;
                            Log.d("val/playerStat:-", stat);

                            if (stat.equals("win")) {
                                winingIDs.add(key);
                            }

                            if (key.equals(mVars.getPlayerID())) {
                                displayResultTv.setText(stat);
                                if (stat.equals("win")) {
                                    playerWinDraw();
                                }
                                if (stat.equals("draw")) {
                                    playerWinDraw();
                                }
                                /**
                                 * delete child if status == "lose"
                                 */
                                if (stat.equals("lose")) {
                                    Log.d("val/insidePhnPLa:-", "delete player plying from phn");
                                    mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).removeValue();
                                    count--;
                                }
                            } else {
                                /**
                                 * delete child if status == "lose"
                                 */
                                if (stat.equals("lose")) {
                                    Log.d("val/insideFBPLa:-", "delete player plying from FB");
                                    mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).removeValue();
                                    count--;
                                }
                            }
                        }
                        /**
                         * if only one player left in game, change status to champion and change status of game to "done"
                         */
                        Log.d("val/Count:-", "" + count);

                        if (count == 1) {
                            Log.d("val/inside????:-", "commented part");
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String key = postSnapshot.getKey();

                                for (int y = 0; y < winingIDs.size(); y++) {
                                    if (key.equals(winingIDs.get(y))) {
                                        Player player = postSnapshot.getValue(Player.class);
                                        player.status = "champion";
                                        displayResultTv.setText(player.status);
                                        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).setValue(player);
                                        mGameDatabase.changeGameStatus("done");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }.start();
    }

    private void playerWinDraw() {
        mGameDatabase.changeGameStatus("waiting");
        countDownTimerTv.setVisibility(View.VISIBLE);

        new CountDownTimer(10000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                countDownTimerTv.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                if (countDownTimerTv.getText().toString().equals("00:00:03")) {
                    Log.d("val/i am inside", "i am inside");
                    readData(new MyCallback() {
                        @Override
                        public void onCallback(String value) {
                            gStatus = value;
                            Log.d("cal/gameStatus:-", "inside readData(interface):- " + gStatus);
                        }
                    });
                }
            }

            public void onFinish() {
                Log.d("val/Lets see :-", "gStatus " + gStatus);
                countDownTimerTv.setText("done!");
                if (gStatus.equals("done")) {
                    countDownTimerTv.setText("You are winner !");
                } else {
                    Log.d("val/PlayerResetStatus", "change stat none");
                    mGameDatabase.RPSreset();
                    mGameDatabase.changeGameStatus("play");

                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CalculateResultActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.calculateResultActivity_quitGame:
                Intent intent = new Intent(CalculateResultActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
                break;
        }
    }

    public interface MyCallback {
        void onCallback(String value);
    }

    public void readData(final MyCallback myCallback) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = String.valueOf(dataSnapshot.getValue());
                Log.d("cal/gameStatus:-", "dataChange method:- " + value);
                myCallback.onCallback(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
