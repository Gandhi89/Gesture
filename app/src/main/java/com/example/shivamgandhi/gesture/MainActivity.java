package com.example.shivamgandhi.gesture;

import android.content.Intent;
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
    Vars mVars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();

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

                /**
                 * Create a new game with one player.
                 */
                mGameDatabase.createGame();
                mGameDatabase.joinPlayer("playerName");
                Log.d("MainActivity/GameID:- ",mVars.getGameID());

                /**
                 * Move to another screen.
                 */
                Intent intent = new Intent(MainActivity.this,WaitingScreen.class);
                intent.putExtra("authorization","yes");
                startActivity(intent);

                break;
            case R.id.mainActivity_joingame:

                Intent i = new Intent(MainActivity.this,EnterGameID.class);
                startActivity(i);
                break;
        }
    }
}
