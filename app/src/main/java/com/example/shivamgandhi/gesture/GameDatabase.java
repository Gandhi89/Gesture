package com.example.shivamgandhi.gesture;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class GameDatabase {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Vars mVars;
    Player mPlayer;
    int point_r = 0;
    int point_p = 0;
    int point_s = 0;
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
        mPlayer = new Player(playerName, "default", "none");
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
     * function to set RPS value
     */
    public void setRPSvalue(String playerName, String value) {
        mVars = Vars.getInstance();
        mPlayer = new Player(playerName, "default", value);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).setValue(mPlayer);
    }

    /**
     * function to calculate which team wins
     */
    public ArrayList<String> calResult(int cnt_r, int cnt_p, int cnt_s, int cnt_none) {

        Log.d("val/cnt_r: ",""+cnt_r);
        Log.d("val/cnt_p: ",""+cnt_p);
        Log.d("val/cnt_s: ",""+cnt_s);
        Log.d("val/cnt_r: ",""+cnt_none);

        point_r = cnt_s;
        point_p = cnt_r;
        point_s = cnt_p;

        Log.d("val/point_r: ",""+point_r);
        Log.d("val/point_p: ",""+point_p);
        Log.d("val/point_s: ",""+point_s);

        int max_point = point_p;
        if (max_point < point_r) {max_point = point_r;}
        if (max_point < point_s) {max_point = point_s;}

        Log.d("val/max_point: ",""+max_point);

        int no_max_point = 0;
        if (point_r == max_point){no_max_point ++;}
        if (point_p == max_point){no_max_point ++;}
        if (point_s == max_point){no_max_point ++;}

        Log.d("val/no_max_point: ",""+no_max_point);

        String winingStatus_rock = "lose";
        String winingStatus_paper = "lose";
        String winingStatus_scissors = "lose";

        // ------------------------------------ //
        if(max_point == (cnt_r + cnt_p + cnt_s + cnt_none))
        {
            winingStatus_rock="draw";
            winingStatus_paper="draw";
            winingStatus_scissors="draw";
            no_max_point = 4;
        }

        // ------------------------------------ //
        if (no_max_point == 1){
            Log.d("val/inside-1: ",""+1);

            if (max_point == cnt_r){winingStatus_rock = "win";Log.d("val/inside-1-R: ",""+1);}
            if (max_point == cnt_p){winingStatus_paper = "win";Log.d("val/inside-1-P: ",""+1);}
            if (max_point == cnt_s){winingStatus_scissors = "win";Log.d("val/inside-1-s: ",""+1);}
            if ((cnt_r > 0) && (winingStatus_scissors.equals("win"))){winingStatus_scissors = "lose"; winingStatus_rock = "win";}
            else if ((cnt_p > 0) && (winingStatus_rock.equals("win"))){winingStatus_rock = "lose"; winingStatus_paper = "win";}
            else if ((cnt_s > 0) && (winingStatus_paper.equals("win"))){winingStatus_paper = "lose"; winingStatus_scissors = "win";}
        }

        // ------------------------------------ //
        else if (no_max_point == 2){
            Log.d("val/inside-2: ",""+2);
            if ((cnt_s > 0) && (point_r == max_point)){winingStatus_scissors = "win";}
            if ((cnt_r > 0) && (point_p == max_point)){winingStatus_rock = "win";}
            if ((cnt_p > 0) && (point_s == max_point)){winingStatus_paper = "win";}
            if ((cnt_r > 0) && (winingStatus_scissors.equals("win"))){winingStatus_scissors = "lose"; winingStatus_rock = "win";}
            if ((cnt_p > 0) && (winingStatus_rock.equals("win"))){winingStatus_rock = "lose"; winingStatus_paper = "win";}
            if ((cnt_s > 0) && (winingStatus_paper.equals("win"))){winingStatus_paper = "lose"; winingStatus_scissors = "win";}
        }

        // ------------------------------------ //
        else if (no_max_point == 3){
            Log.d("val/inside-3: ",""+3);
            if (cnt_none > 0){
                winingStatus_scissors = "win";
                winingStatus_rock = "win";
                winingStatus_paper = "win";
            }
            else {
                Log.d("val/inside-3-draw: ",""+3);
                winingStatus_scissors = "draw";
                winingStatus_rock = "draw";
                winingStatus_paper = "draw";
            }
        }

        winingStatus.add(winingStatus_rock);
        winingStatus.add(winingStatus_paper);
        winingStatus.add(winingStatus_scissors);

        Log.d("val/Rock:- ",winingStatus_rock);
        Log.d("val/Paper:- ",winingStatus_paper);
        Log.d("val/Scissors:- ",winingStatus_scissors);
        return winingStatus;
    }
}
