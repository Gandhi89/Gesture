package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EnterGameID extends AppCompatActivity implements View.OnClickListener {

    Button goBtn;
    EditText gameIDet;
    Vars mVars;
    GameDatabase mGameDatabase;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    ArrayList<String> gameIDs;
    ArrayList<String> gameStatus;
    int[] numbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_game_id);
        mGameDatabase = new GameDatabase();
        goBtn = findViewById(R.id.EnterGameID_go);
        gameIDet = findViewById(R.id.EnterGameID_gameid);
        gameIDs = new ArrayList<>();
        gameStatus = new ArrayList<>();


        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mDatabaseReference.child("game").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postData: dataSnapshot.getChildren()){


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        goBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.EnterGameID_go:
                if (gameIDet.getText().toString() != "") {
                    /**
                     * get value from editText add it to database
                     * TODO:- check if gameID exist or not
                     * TODO:- check status of game before entering
                     */
                    mVars.setGameID(gameIDet.getText().toString());
                    mVars.setPlayerID(mGameDatabase.createPlayer(mVars.getPlayerName(),mVars.getLat(),mVars.getLog()));
                    Log.d("EnterGameID/PlayerID:- ", mVars.getPlayerID());
                    /**
                     * since user pressed "join game", set authorization to "no".
                     */
                    Intent i = new Intent(EnterGameID.this, WaitingScreen.class);
                    i.putExtra("authorization", "no");
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Enter GameID", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
