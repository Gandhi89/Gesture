package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaitingScreen extends AppCompatActivity implements View.OnClickListener {

    private String authorization = "";
    GameDatabase mGameDatabase;
    Button startGameBtn;
    TextView gameIDtv;
    TextView playerListTv;
    Vars mVars;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_screen);

        Intent i = getIntent();
        authorization = i.getStringExtra("authorization");

        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();

        playerListTv = findViewById(R.id.waitingscreen_playerList);
        startGameBtn = findViewById(R.id.waitingscreen_startGame);
        gameIDtv = findViewById(R.id.waitingscreen_gameid);
        gameIDtv.setText("GameID:- " + mVars.getGameID());

        /**
         * only show button if user created game
         */
        if (authorization.equals("yes")) {
            startGameBtn.setVisibility(View.VISIBLE);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addChildEventListener(new playerAddedListner());
        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").addValueEventListener(new watchStatus());

        startGameBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.waitingscreen_startGame:
                /**
                 * change status of game to "play"
                 */
                mGameDatabase.changeStatus("play");
                Intent i = new Intent(WaitingScreen.this, HomeActivity.class);
                startActivity(i);
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
            playerListTv.append(player.name);
            playerListTv.append("\n");
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
}
