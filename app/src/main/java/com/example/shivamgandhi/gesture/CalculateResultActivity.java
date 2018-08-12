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

    List<String> userChoice = new ArrayList<>();

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Vars mVars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_result);

        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

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
            }
        }.start();
    }
}
