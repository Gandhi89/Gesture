package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button createGameBtn, joinGameBtn;
    GameDatabase mGameDatabase;
    Vars mVars;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int RC_SIGN_IN = 1;
    String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    Log.d("MainActivity/log","5");
                    // user is signed in
                    onSignInInitiaize(user.getDisplayName());
                    Toast.makeText(MainActivity.this, "You are now sign in.Welcome!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d("MainActivity/log","6");

                    // user is signed out
                    onSignOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
        Log.d("MainActivity/log","0");
        if (getIntent().getBooleanExtra("EXIT", false)) {
            Log.d("MainActivity/log","1");

            finish();
            return;
        }
        Log.d("MainActivity/log","2");

        setContentView(R.layout.activity_main);
        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();

        createGameBtn = findViewById(R.id.mainActivity_creategame);
        joinGameBtn = findViewById(R.id.mainActivity_joingame);


        createGameBtn.setOnClickListener(this);
        joinGameBtn.setOnClickListener(this);
        Log.d("MainActivity/log","3");

        /**
         * check if user is signed in or not
         */

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mainActivity_creategame:

                /**
                 * Create a new game with one player.
                 */
                mGameDatabase.createGame();
                mVars.setPlayerID(mGameDatabase.createPlayer("playerName"));
                Log.d("MainActivity/GameID:- ", mVars.getGameID());
                Log.d("MainActivity/PlayerID:-", mVars.getPlayerID());

                /**
                 * Since player created game, set authorization to "yes".
                 */
                Intent intent = new Intent(MainActivity.this, WaitingScreen.class);
                intent.putExtra("authorization", "yes");
                startActivity(intent);

                break;
            case R.id.mainActivity_joingame:

                Intent i = new Intent(MainActivity.this, EnterGameID.class);
                startActivity(i);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity/log","onResume");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity/log","onPause");
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void onSignInInitiaize(String userName){

        mUserName = userName;

    }
    private void onSignOutCleanUp(){

        mUserName = "";

    }
}
