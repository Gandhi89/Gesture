package com.example.shivamgandhi.gesture;

public class Vars {
    private static Vars singleton = new Vars();

    private Vars() {
    }

    public static Vars getInstance() {
        return singleton;
    }

    // server side
    String gameID = "";

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }
}
