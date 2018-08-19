package com.example.shivamgandhi.gesture;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button createGameBtn, joinGameBtn;
    GameDatabase mGameDatabase;
    Vars mVars;
    User mUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int RC_SIGN_IN = 1;
    String title = "beginner";
    double lat = 43.2323, log = -79.2323;
    ArrayList<String> emails;
    ArrayList<String> primaeyKey;
    boolean isRegistered = false;

    private PermissionListener mPermissionListener;

    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);


        emails = new ArrayList<>();
        primaeyKey = new ArrayList<>();
        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();
        mUser = new User();

        emails = mVars.getRegisteredUsers();
        Log.d("MainAct/users",mVars.getRegisteredUsers().toString());
        primaeyKey = mVars.getUserPrimaryKey();

        createGameBtn = findViewById(R.id.mainActivity_creategame);
        joinGameBtn = findViewById(R.id.mainActivity_joingame);


        createGameBtn.setOnClickListener(this);
        joinGameBtn.setOnClickListener(this);

        /**
         * check if user is signed in or not
         */
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("MainAct/onResume?",emails.toString());

                    for (int i = 0;i<emails.size();i++){
                        if (user.getEmail().equals(emails.get(i))){
                            Log.d("MainAct/foundUser", "true");
                            mVars.setPlayerName(user.getDisplayName());
                            isRegistered = true;

                            mVars.setPrimarykey(primaeyKey.get(i));

                            /**
                             * setup Vars class
                             */
                            mGameDatabase.setupUserClass(primaeyKey.get(i));

                            Log.d("MainAct/UserDetail", ">>>>>>>");
                            Log.d("MainAct/UserPk", mVars.getPrimarykey());
//                            Log.d("MainAct/UserTit", mVars.getTitle());
                            Log.d("MainAct/UserName", mVars.getPlayerName()+"");
                           // Log.d("MainAct/UserStat", mVars.getStatus());
                            Log.d("MainAct/UserLat", mVars.getLat()+"");
                            Log.d("MainAct/UserLog", mVars.getLog()+"");
               //             Log.d("MainAct/UserWin", mVars.getWining()+"");
                            Log.d("MainAct/UserDetail", ">>>>>>>");

                        }

                    }
                    /**
                     * user is signed in
                     */
                    if (isRegistered){
                        Log.d("MainActivity/email","user already registered");
                        /**
                         * update current location and change status to online
                         */
                        Log.d("MainAct/updateLocation", ">>>>>>>");
                        mGameDatabase.updateCurrentLocation(mVars.getLat(),mVars.getLog());
                        Log.d("MainAct/onlineStatus", ">>>>>>>");
                        mGameDatabase.updateUserStatus("online");

                    }
                    else {
                        Log.d("MainAct/UnregiredUser", ">>>>>>>");
                        mVars.setTitle("beginner");
                        mVars.setWining(0);
                        mVars.setStatus("online");
                        onSignInInitiaize(user.getEmail(), user.getDisplayName(), mVars.getWining(), mVars.getTitle(), mVars.getLat(), mVars.getLog(), mVars.getStatus());
                    }

                } else {

                    // user is signed out
                    Log.d("MainAct/UserSignOut", ">>>>>>>");
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
        if (getIntent().getBooleanExtra("EXIT", false)) {
            Log.d("MainActivity/log", "1");
            AuthUI.getInstance().signOut(this);
            finish();
            return;
        }

        /**
         * get location in every 10 sec
         */
        getCurrent();
        /**
         * update location in every 10 sec
         */
        updateUserLocation();

    } // end of onCreate()





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d("MainAct/ResultOk", ">>>>>>>");
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
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
                mGameDatabase.createGame(mVars.getLat(),mVars.getLog());
                mVars.setPlayerID(mGameDatabase.createPlayer(mVars.getPlayerName(),mVars.getLat(),mVars.getLog()));
                Log.d("MainActivity/GameID:- ", mVars.getGameID());
                Log.d("MainActivity/PlayerID:-", mVars.getPlayerID());

                /**
                 * Since player created game, set authorization to "yes".
                 */
                Intent intent = new Intent(MainActivity.this, WaitingScreen.class);
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
        Log.d("MainActivity/log", "onResume");
       // Log.d("MainActivity/onRes", mVars.getPrimarykey());
        mGameDatabase.updateUserStatus("online");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity/log", "onPause");
       // Log.d("MainActivity/onPau", mVars.getPrimarykey());
        mGameDatabase.updateUserStatus("offline");
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

    }

    private void onSignInInitiaize(String email, String name, int wining, String title, double lat, double log, String status) {

        Log.d("MainAct/NewEntry", ">>>>>>>");
        mVars.setPlayerName(name);
        mVars.setPrimarykey(mGameDatabase.addUser(email, mVars.getPlayerName(), wining, title, lat, log, status));
        emails.add(email);
        primaeyKey.add(mVars.getPrimarykey());
        mVars.setRegisteredUsers(emails);
        mVars.setUserPrimaryKey(primaeyKey);
        mGameDatabase.setupUserClass(mVars.getPrimarykey());
    }

    private void onSignOutCleanUp() {

    }

    /**
     * get location stuff
     */

    public void getCurrent() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // set up the location request to
        // ask for new location every 10 seconds
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); // 10 second interval
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        // setup permission listener
        createPermissionListener();

        // request permissions
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(mPermissionListener)
                .check();
        update_location();

    }

    public void createPermissionListener() {
        mPermissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                Log.d("MainActivity/lctn is:-","PERMISSION GRANTED!");
                createLocationCallback();
                getLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            }
        };
    }

    public void update_location() {
        Log.d("MainActivity/lctn is:-", "location updates pressed");
        createLocationCallback();
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
        catch (SecurityException e) {
            Log.d("MainActivity/lctn is:-", "Exception during loc updates: " + e.toString());
            Log.d("MainActivity/lctn is:-", "Exception during loc updates: " + e.toString());
        }
    }

    public void createLocationCallback() {
        if (mLocationCallback == null) {

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.d("MainActivity/location", "Location callback - location is null, exiting");
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        double lat = location.getLatitude();
                        double log = location.getLongitude();
                        mVars.setLat(lat);
                        mVars.setLog(log);

                    }
                };
            };
        }
    }
    public void getLocation() {
        Log.d("MainActivity/location", "trying to get location");
        //Log.d("MainActivity/location", "trying to get location");
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.d("MainActivity/lctn is:-", "getting last known location");
                                Log.d("MainActivity/lctn is:-", "Lat:- " + location.getLatitude());
                                Log.d("MainActivity/lctn is:-", "Long:- " + location.getLongitude());
                                double lat = location.getLatitude();
                                double log = location.getLongitude();
                                mVars.setLat(lat);
                                mVars.setLog(log);


                            }
                            else {
                                Log.d("MainActivity/lctn:-", "last locaiton is null");
                                mVars.setLat(00.00);
                                mVars.setLog(00.00);
                            }
                        }
                    });
        }
        catch (SecurityException e) {
            Log.d("MainActivity/lctn is:-","CATCH IS NOW");
            Log.d("MainActivity/lctn is:-",e.toString());
        }
    }

    /**
     * update location in 10 sec
     */
    private void updateUserLocation() {
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                mGameDatabase.updateCurrentLocation(mVars.getLat(),mVars.getLog());
            }
        }.start();
    }
}
