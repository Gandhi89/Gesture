package com.example.shivamgandhi.gesture;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EnterGameID extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Vars mVars;
    ListView gamesLv;
    GameDatabase mGameDatabase;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    ArrayList<String> gameIDs;
    ArrayList<String> gameStatus;
    ArrayList<String> displayGameIDs;
    Game mGame;
    DisplayGameCustomAdapter mDisplayGameCustomAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_game_id);

        /**
         * show progress dialog
         */
        showProgressDialog();

        initializwAll();
        gamesLv = findViewById(R.id.enterGameID_listview);
        mDisplayGameCustomAdapter = new DisplayGameCustomAdapter(EnterGameID.this, displayGameIDs);
        gamesLv.setAdapter(mDisplayGameCustomAdapter);

        /**
         * get Games and update ListView
         */
        getGames();

        gamesLv.setOnItemClickListener(this);

    }// end of onCreate()

    private void showProgressDialog() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.show();
        progress.setCancelable(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress.dismiss();
            }
        }, 5000);
    }

    private void getGames() {

        mDatabaseReference.child("game").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot postData : dataSnapshot.getChildren()) {
                        Log.d("EnterId/allData",postData.toString());

                        Double lat=(Double) postData.child("latitude").getValue();
                        Double log= (Double)postData.child("longitude").getValue();

                        //Double log = game.log;

                        Log.d("EnterId/gameLat", "" + lat);
                        Log.d("EnterId/gameLong", "" + log);
                        LatLng latLngA = new LatLng(lat, log);
                        LatLng latLngB = new LatLng(mVars.getLat(), mVars.getLog());

                        Location locationA = new Location(LocationManager.GPS_PROVIDER);
                        locationA.setLatitude(latLngA.latitude);
                        locationA.setLongitude(latLngA.longitude);
                        Location locationB = new Location(LocationManager.GPS_PROVIDER);
                        locationB.setLatitude(latLngB.latitude);
                        locationB.setLongitude(latLngB.longitude);


                        double distance = locationA.distanceTo(locationB);
                        Log.d("EnterId/distnace", "FINAL DISTANCE:- " + distance);

                        if (distance < 2000) {
                            displayGameIDs.add(postData.getKey());
                            Log.d("EnterId/game",displayGameIDs.toString());
                            mDisplayGameCustomAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void initializwAll() {
        mGameDatabase = new GameDatabase();
        gameIDs = new ArrayList<>();
        gameStatus = new ArrayList<>();
        displayGameIDs = new ArrayList<>();
        mGame = new Game();

        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String game = displayGameIDs.get(i);
        mVars.setGameID(game);
        mVars.setPlayerID(mGameDatabase.createPlayer(mVars.getPlayerName(),mVars.getLat(),mVars.getLog()));
        Intent intent = new Intent(EnterGameID.this,WaitingScreen.class);
        startActivity(intent);

    }
}
