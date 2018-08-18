package com.example.shivamgandhi.gesture;

import java.util.ArrayList;

public class Vars {
    private static Vars singleton = new Vars();

    private Vars() {
    }

    public static Vars getInstance() {
        return singleton;
    }

    // server side
    private String gameID = "";

    // user side
    private String playerName = "";
    private String playerID = "";

    // USER CALSS FROM DATABASE
    private ArrayList<String> registeredUsers = new ArrayList<>();
    private ArrayList<String> userPrimaryKey = new ArrayList<>();
    private String primarykey;
    private double lat;
    private double log;
    private String title;
    private int wining;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getWining() {
        return wining;
    }

    public void setWining(int wining) {
        this.wining = wining;
    }



    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public String getPrimarykey() {
        return primarykey;
    }

    public void setPrimarykey(String primarykey) {
        this.primarykey = primarykey;
    }


    public ArrayList<String> getUserPrimaryKey() {
        return userPrimaryKey;
    }

    public void setUserPrimaryKey(ArrayList<String> userPrimaryKey) {
        this.userPrimaryKey = userPrimaryKey;
    }


    public ArrayList<String> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(ArrayList<String> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

}
