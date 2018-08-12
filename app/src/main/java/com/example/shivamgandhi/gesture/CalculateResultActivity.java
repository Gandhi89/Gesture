package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalculateResultActivity extends AppCompatActivity {

    ArrayList<String> userChoice = new ArrayList<>();

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    int count_r=0,count_p=0,count_s=0;
    ArrayList<String> winingStatus = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_result);

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
                Log.d("calculateResult/res ",winingStatus.toString());
                Toast.makeText(CalculateResultActivity.this, winingStatus.toString(), Toast.LENGTH_SHORT).show();
            }
        }.start();
    }
}
