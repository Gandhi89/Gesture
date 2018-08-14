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

public class CalculateResultActivity extends AppCompatActivity implements View.OnClickListener {

    TextView displayResultTv;
    Button nxtRoundBtn, quitGameBtn;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    String stat;

    ArrayList<String> userChoice = new ArrayList<>();
    ArrayList<String> winingIDs = new ArrayList<>();
    int count_r = 0, count_p = 0, count_s = 0, count_none = 0;
    String getWiningStatus_r, getWiningStatus_p, getWiningStatus_s;
    ArrayList<String> winingStatus = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_result);

        displayResultTv = findViewById(R.id.calculateResultActivity_textview);
        nxtRoundBtn = findViewById(R.id.calculateResultActivity_nextRound);
        quitGameBtn = findViewById(R.id.calculateResultActivity_quitGame);
        quitGameBtn.setOnClickListener(this);
        nxtRoundBtn.setOnClickListener(this);

        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mGameDatabase = new GameDatabase();

        /**
         * get RPS of all players
         */
        /**
         * TODO :- what if child is added but that player does not respond ?????
         */
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userChoice.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Player player = postSnapshot.getValue(Player.class);
                    userChoice.add(player.RPS);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /**
         *  wait for 7 sec, SYNC purpose
         */
        new CountDownTimer(7000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Toast.makeText(CalculateResultActivity.this, userChoice.toString(), Toast.LENGTH_SHORT).show();
                /**
                 * calculate count_r,count_p,count_s and call 'calResult' method
                 */
                Log.d("CalculateResult/array:-", userChoice.toString());
                for (int i = 0; i < userChoice.size(); i++) {
                    if (userChoice.get(i).equals("rock")) {
                        count_r++;
                        Log.d("CalculateResult/cnt_r:-", "" + count_r);
                    } else if (userChoice.get(i).equals("paper")) {
                        count_p++;
                        Log.d("CalculateResult/cnt_p:-", "" + count_p);
                    } else if (userChoice.get(i).equals("dummy")) {
                        count_p++;
                        Log.d("CalculateResult/dummy:-", "" + count_p);
                    } else if (userChoice.get(i).equals("scissors")) {
                        count_s++;
                        Log.d("CalculateResult/cnt_s:-", "" + count_s);
                    } else {
                        count_none++;
                        Log.d("CalculateResult/cnt_n:-", "" + count_none);
                    }
                }
                winingStatus = mGameDatabase.calResult(count_r, count_p, count_s, count_none);
                Toast.makeText(CalculateResultActivity.this, winingStatus.toString(), Toast.LENGTH_SHORT).show();
                /**
                 * set winingStatus of team R, team P, team S
                 */
                Log.d("Calcula/winingStatus:-", winingStatus.toString());
                getWiningStatus_r = winingStatus.get(0);
                getWiningStatus_p = winingStatus.get(1);
                getWiningStatus_s = winingStatus.get(2);

                /**
                 * this Listener is called only once to get RPS data
                 */
                mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Toast.makeText(CalculateResultActivity.this, "inside", Toast.LENGTH_SHORT).show();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Player player = postSnapshot.getValue(Player.class);

                            /**
                             * change status[win/lose] of player in Player class
                             */
                            if (player.RPS.equals("rock")) {
                                player.status = getWiningStatus_r;
                                Log.d("Calcula/np/R", player.status);
                            } else if (player.RPS.equals("paper")) {
                                player.status = getWiningStatus_p;
                                Log.d("Calcula/np/P", player.status);
                            } else if (player.RPS.equals("scissors")) {
                                player.status = getWiningStatus_s;
                                Log.d("Calcula/np/S", player.status);
                            } else {
                                player.status = "lose";
                            }

                            String key = postSnapshot.getKey();
                            /**
                             * update the status of player in Firebase database
                             */
                            mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).setValue(player);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                /**
                 * display result on user screen
                 */
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

                            if (stat.equals("win")){ winingIDs.add(key); }

                            if (key.equals(mVars.getPlayerID())) {
                                displayResultTv.setText(stat);
                                if (stat == "win") {
                                    Log.d("val/setVisibile:-", "show button");
                                    nxtRoundBtn.setVisibility(View.VISIBLE);
                                }
                                /**
                                 * delete child if status == "lose"
                                 */
                                if (stat.equals("lose")) {
                                    Log.d("val/insidePhnPLa:-", "delete player plying from phn");
                                    mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).removeValue();
                                    count --;
                                }
                            } else {
                                /**
                                 * delete child if status == "lose"
                                 */
                                if (stat.equals("lose")) {
                                    Log.d("val/insideFBPLa:-", "delete player plying from FB");
                                    mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).removeValue();
                                    count --;
                                }
                            }
                        }
                        /**
                         * if only one player left in game, change status to champion and change status of game to "done"
                         */
                        Log.d("val/Count:-", ""+count);

                       if (count == 1)
                        {
                        Log.d("val/inside????:-", "commented part");
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String key = postSnapshot.getKey();

                                for (int y=0;y<winingIDs.size();y++){
                                    if (key.equals(winingIDs.get(y))){
                                        Player player = postSnapshot.getValue(Player.class);
                                        player.status = "champion";

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
            case R.id.calculateResultActivity_nextRound:
                Intent i = new Intent(CalculateResultActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
        }
    }

}
