package com.example.shivamgandhi.gesture;

public class Player {

    public String name;
    public String status;
    public String RPS;
    public String ready;
    public double lat;
    public double log;

    public Player() {

    }

    public Player(String n, String e, String w, String p, Double q, Double r) {
        this.name = n;
        this.status = e;
        this.RPS = w;
        this.ready = p;
        lat = q;
        log = r;
    }

}
