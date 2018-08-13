package com.example.shivamgandhi.gesture;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class GameDatabase {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Vars mVars;
    Player mPlayer;
    int point_r=0;
    int point_p=0;
    int point_s=0;
    ArrayList<String> winingStatus = new ArrayList<>(); // IN ORDER OF RPS

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
     * function to create player and join into existing game
     */
    public String createPlayer(String playerName) {
        mVars = Vars.getInstance();
        mVars.setPlayerName(playerName);
        mPlayer = new Player(playerName,"default","none");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        DatabaseReference games = mDatabaseReference.child("game");
        DatabaseReference game = games.child(mVars.getGameID());
        DatabaseReference players = game.child("players");
        DatabaseReference player = players.push();
        player.setValue(mPlayer);

        return player.getKey();
        //mDatabaseReference.child("game").child(mVars.getGameID()).child("players").push().setValue(mPlayer);
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
    /**
     *  function to set RPS value
     */
    public void setRPSvalue(String playerName,String value){
        mVars = Vars.getInstance();
        mPlayer = new Player(playerName,"default",value);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).setValue(mPlayer);
    }

    /**
     * function to calculate which team wins
     */
    public ArrayList<String> calResult(int cnt_r, int cnt_p, int cnt_s){

        point_p = cnt_r;
        point_r = cnt_s;
        point_s = cnt_p;

        int max_point = point_p;
        if (max_point<point_r){
            max_point = point_r;
        }
        if (max_point<point_s){
            max_point = point_s;
        }
        String winingStatus_rock="lose";
        String winingStatus_paper="lose";
        String winingStatus_scissors="lose";

        if (point_r == max_point)
        {
            winingStatus_rock = "win";
        }
        if (point_s == max_point)
        {
            winingStatus_scissors = "win";
        }
        if (point_p == max_point)
        {
            winingStatus_paper = "win";
        }
        if((point_r == point_p) && (point_p  == point_s) && (point_s == max_point))
        {
            winingStatus_rock = "draw";
            winingStatus_scissors = "draw";
            winingStatus_paper = "draw";
        }

        winingStatus.add(winingStatus_rock);
        winingStatus.add(winingStatus_paper);
        winingStatus.add(winingStatus_scissors);

        return winingStatus;
    }
}
