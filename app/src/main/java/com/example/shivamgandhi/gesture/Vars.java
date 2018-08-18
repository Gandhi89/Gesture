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
