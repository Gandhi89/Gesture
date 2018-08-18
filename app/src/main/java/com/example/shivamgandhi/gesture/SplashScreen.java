package com.example.shivamgandhi.gesture;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SplashScreen extends AppCompatActivity implements OnMapReadyCallback{


    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    User mUser;
    ArrayList<String> userPrimaryKeys;
    ArrayList<String> emails ;
    HashMap<String, Object> loc;
    private GoogleMap mMap;
    private PermissionListener mPermissionListener;

    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        emails = new ArrayList<>();
        userPrimaryKeys = new ArrayList<>();
        mVars = Vars.getInstance();
        mUser = new User();
        mVars = Vars.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mGameDatabase = new GameDatabase();

        getRegisteredUser();
        /**
         * TODO:- GET USER'S CURRENT LOCATION
         */
        getCurrent();

        final Intent myintent = new Intent(this, MainActivity.class);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                mVars.setRegisteredUsers(emails);
                mVars.setUserPrimaryKey(userPrimaryKeys);
                Log.d("splash",mVars.getUserPrimaryKey().toString());
                Log.d("splash",mVars.getRegisteredUsers().toString());
                startActivity(myintent);
                finish();
            }
        }, 4000);
    }

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
                        Log.d("MainActivity/location","Location callback - found locations");
                        Log.d("MainActivity/lat is:- ", location.getLatitude()+"");
                        Log.d("MainActivity/long is:- ", location.getLongitude()+"");
                        double lat = location.getLatitude();
                        double log = location.getLongitude();
                        mVars.setLat(lat);
                        mVars.setLog(log);

                    }
                };
            };
        }
    }

    /**
     * method to get all User's Email
     */
    private void getRegisteredUser() {

        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postData:dataSnapshot.getChildren()){
                    User usr = postData.getValue(User.class);
                    emails.add(usr.Email);
                    userPrimaryKeys.add(postData.getKey());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
