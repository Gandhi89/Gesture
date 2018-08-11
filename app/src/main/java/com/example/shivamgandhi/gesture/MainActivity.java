package com.example.shivamgandhi.gesture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button createGameBtn,joinGameBtn;
    GameDatabase mGameDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGameDatabase = new GameDatabase();

        createGameBtn = findViewById(R.id.mainActivity_creategame);
        joinGameBtn = findViewById(R.id.mainActivity_joingame);


        createGameBtn.setOnClickListener(this);
        joinGameBtn.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.mainActivity_creategame:

                Log.d("MainActivity/GameID:- ",mGameDatabase.createGame("PlayerName"));
                break;
            case R.id.mainActivity_joingame:

                break;
        }
    }
}
