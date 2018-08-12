package com.example.shivamgandhi.gesture;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class GameDatabase {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Vars mVars;
    Player mPlayer;

    public void GameDatabase() {
    }

    /**
     * function to create new game
     */
    public void createGame() {
        mVars = Vars.getInstance();
        mVars.setGameID(generateGameID());

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").setValue("waiting");
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players");

    }

    /**
     * function to join into existing game
     */
    public void joinPlayer(String playerName) {
        mVars = Vars.getInstance();
        mPlayer = new Player(playerName,"playing");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").push().setValue(mPlayer);
    }


    /**
     * function to generate 6 digit long random number for unique gameid
     */
    public String generateGameID() {
        Random rnd = new Random();
        int number = 100000 + rnd.nextInt(900000);
        return String.valueOf(number);
    }

    /**
     * function to change status of game
     */
    public void changeStatus(String status) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").setValue(status);

    }

}
