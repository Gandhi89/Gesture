package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalculateResultActivity extends AppCompatActivity implements View.OnClickListener {

    TextView displayResultTv;
    Button nxtRoundBtn,quitGameBtn;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    String stat;

    ArrayList<String> userChoice = new ArrayList<>();
    int count_r=0,count_p=0,count_s=0;
    String winingStatus_r,getWiningStatus_p,getWiningStatus_s;
    ArrayList<String> winingStatus = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_result);

        displayResultTv = findViewById(R.id.calculateResultActivity_textview);
        nxtRoundBtn = findViewById(R.id.calculateResultActivity_nextRound);
        quitGameBtn = findViewById(R.id.calculateResultActivity_quitGame);
        quitGameBtn.setOnClickListener(this);

        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mGameDatabase = new GameDatabase();

        /**
         * get RPS of all players
         */
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userChoice.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Player player = postSnapshot.getValue(Player.class);
                    userChoice.add(player.RPS);
                    Log.d("CalculateResult/RPS:-",player.RPS);
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
                for (int i=0; i<userChoice.size(); i++){
                    if (userChoice.get(i) == "rock"){
                        count_r ++;
                    }
                    else if (userChoice.get(i) == "paper"){
                        count_p ++;
                    }
                    else if (userChoice.get(i) == "scissors"){
                        count_s ++;
                    }
                }
                winingStatus = mGameDatabase.calResult(count_r,count_p,count_s);
                Toast.makeText(CalculateResultActivity.this, winingStatus.toString(), Toast.LENGTH_SHORT).show();
                /**
                 * set winingStatus of team R, team P, team S
                 */
                winingStatus_r = winingStatus.get(0);
                getWiningStatus_p = winingStatus.get(1);
                getWiningStatus_s = winingStatus.get(2);

                /**
                 * this Listener is called only once to get RPS data
                 */
                mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Toast.makeText(CalculateResultActivity.this, "inside", Toast.LENGTH_SHORT).show();
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Player player = postSnapshot.getValue(Player.class);

                            /**
                             * change status[win/lose] of player in Player class
                             */
                            if (player.RPS.equals("rock")){
                                player.status = winingStatus_r;
                            }
                            else if(player.RPS.equals("paper")){
                                player.status = getWiningStatus_p;
                            }
                            else if(player.RPS.equals("scissors")){
                                player.status = getWiningStatus_s;
                            }
                            else {
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
        new CountDownTimer(5000,1000){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            String key = postSnapshot.getKey();

                            if(key.equals(mVars.getPlayerID())){

                                Player player = postSnapshot.getValue(Player.class);
                                stat = player.status;
                                displayResultTv.setText(stat);
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
        switch (view.getId()){
            case R.id.calculateResultActivity_quitGame:
                Intent intent = new Intent(CalculateResultActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
                break;
        }
    }
}
