package com.example.shivamgandhi.gesture;

import android.Manifest;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.seismic.ShakeDetector;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity implements ShakeDetector.Listener, View.OnClickListener {

    private int shakeCount = 0;
    private static final String FORMAT = "%02d:%02d:%02d";
    ImageView iv;
    Button btn;
    TextView timer;
    GameDatabase mGameDatabase;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Vars mVars;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        iv = findViewById(R.id.homeActivity_imageView);
        timer = findViewById(R.id.homeActivity_timer);
        btn = findViewById(R.id.homeActivity_button);
        btn.setVisibility(View.INVISIBLE);

        btn.setOnClickListener(this);
        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();

        /**
         * timer
         */
        new CountDownTimer(10000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                timer.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                timer.setText("done!");
                Intent i = new Intent(HomeActivity.this, CalculateResultActivity.class);
                startActivity(i);
            }
        }.start();
        handler = new Handler();
        handler.postDelayed(runnable, 10000);
    } // end of onCreate()

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            Log.d("handler","calling in every 10 sec");
            checkInRange();

            handler.postDelayed(this, 10000);
        }
    };

    /**
     * This method will be called after every shake.
     */
    public void hearShake() {
        shakeCount++;

        if (shakeCount < 6) {
            Toast.makeText(this, "Shake Count :- " + shakeCount, Toast.LENGTH_SHORT).show();
        }
        /**
         *  update RPS value of user
         */
        else if (shakeCount == 6) {
            Random rm = new Random();
            int numberGenerated = rm.nextInt(3);
            if (numberGenerated == 0) {
                iv.setImageResource(R.drawable.rock);
                mGameDatabase.setRPSvalue(mVars.getPlayerName(), "rock");
                //btn.setVisibility(View.VISIBLE);
            } else if (numberGenerated == 1) {
                iv.setImageResource(R.drawable.paper);
                mGameDatabase.setRPSvalue(mVars.getPlayerName(), "paper");
                //btn.setVisibility(View.VISIBLE);
            } else if (numberGenerated == 2) {
                iv.setImageResource(R.drawable.scissors);
                mGameDatabase.setRPSvalue(mVars.getPlayerName(), "scissors");
                //btn.setVisibility(View.VISIBLE);
            }
            shakeCount = 0;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.homeActivity_button:

                /**
                 * Reset the game
                 */
                iv.setImageResource(R.drawable.rpc);
                shakeCount = 0;
                btn.setVisibility(View.INVISIBLE);

                break;
        }
    }

    private void checkInRange() {

        mDatabaseReference.child("game").child(mVars.getGameID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double lat = (Double) dataSnapshot.child("latitude").getValue();
                Double log = (Double) dataSnapshot.child("longitude").getValue();


                LatLng latLngA = new LatLng(lat, log);
                LatLng latLngB = new LatLng(mVars.getLat(), mVars.getLog());

                Location locationA = new Location(LocationManager.GPS_PROVIDER);
                locationA.setLatitude(latLngA.latitude);
                locationA.setLongitude(latLngA.longitude);
                Location locationB = new Location(LocationManager.GPS_PROVIDER);
                locationB.setLatitude(latLngB.latitude);
                locationB.setLongitude(latLngB.longitude);

                double distance = locationA.distanceTo(locationB);
                Log.d("waitingScreen/distnace", "FINAL DISTANCE:- " + distance);

                if (distance > 500) {
                    Toast.makeText(HomeActivity.this, "Game is not in range", Toast.LENGTH_SHORT).show();
                    mGameDatabase.removePlayerFromGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}

