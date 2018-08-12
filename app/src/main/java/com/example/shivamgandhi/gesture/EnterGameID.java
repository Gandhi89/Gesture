package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EnterGameID extends AppCompatActivity implements View.OnClickListener {

    Button goBtn;
    EditText gameIDet;
    Vars mVars;
    GameDatabase mGameDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_game_id);
        mGameDatabase = new GameDatabase();
        goBtn = findViewById(R.id.EnterGameID_go);
        gameIDet = findViewById(R.id.EnterGameID_gameid);

        mVars = Vars.getInstance();

        goBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.EnterGameID_go:
                if (gameIDet.getText().toString() != "") {
                    /**
                     * get value from editText add it to database
                     */
                    mVars.setGameID(gameIDet.getText().toString());
                    mVars.setPlayerID(mGameDatabase.createPlayer("NickName"));
                    Log.d("EnterGameID/PlayerID:- ",mVars.getPlayerID());
                    /**
                     * since user pressed "join game", set authorization to "no".
                     */
                    Intent i = new Intent(EnterGameID.this, WaitingScreen.class);
                    i.putExtra("authorization", "no");
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Enter GameID", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
