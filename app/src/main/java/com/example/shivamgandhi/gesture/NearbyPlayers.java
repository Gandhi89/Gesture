package com.example.shivamgandhi.gesture;

import android.app.ProgressDialog;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class NearbyPlayers extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    ArrayList<String> playerEmail;
    ArrayList<String> playerName;
    ArrayList<Double> latitude;
    ArrayList<Double> logitude;
    private GoogleMap mMap;
    Marker marker;
    boolean b = true;
    int cnt = 0;
    private PermissionListener mPermissionListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_players);

        showProgressDialog();
        initializeAll();
        mGameDatabase.updateUserStatus("online");
        getAllActivePlayers();
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                loadMap();
            }
        }.start();

        /**
         * TODO:- ADD MAP AND DISPLAY LOCATION WITH MARKER
         */


    }// end of onCreate()

    private void loadMap() {
        if (mMap != null) {
            if (marker != null) {
                marker.remove();
            }
            Log.d("inside", playerName.toString());
            for (int i = 0; i < playerName.size(); i++) {

                double lat = latitude.get(i);
                double lon = logitude.get(i);
                LatLng mapMe = new LatLng(lat, lon);


                marker =
                        mMap.addMarker(new MarkerOptions().position(mapMe).title(playerName.get(i)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mapMe));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));


            }


        } else {
            Log.d("HARSH", "NULL");
            LatLng sydney = new LatLng(151, -34);
            mMap.addMarker(new MarkerOptions().position(sydney).title("No Users AVailable"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }

    }

    private void getAllActivePlayers() {

        mDatabaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerName.clear();
                playerEmail.clear();
                latitude.clear();
                logitude.clear();

                Log.d("NBP",dataSnapshot.getValue().toString());
                Log.d("PEMAIL",playerEmail.toString());
                Log.d("counting",playerEmail.toString());

                for (DataSnapshot postData : dataSnapshot.getChildren()){

                    User user = postData.getValue(User.class);
                    String email = user.Email;
                    String stat = user.Status;
                    String name = user.userName;
                    Double lat = user.lat;
                    Double log = user.log;

                    if (stat.equals("online")) {
                        Log.d("NearByPLayer/add11", "online");

                        if(playerEmail.size() == 0){
                            playerEmail.add(email);
                            playerName.add(name);
                            latitude.add(lat);
                            logitude.add(log);
                        }
                        else {
                            for(int p = 0;p<playerEmail.size();p++){
                                if (email.equals(playerEmail.get(p))){
                                    b = false;
                                }
                            }

                        }
                       if(b){
                           playerEmail.add(email);
                           playerName.add(name);
                           latitude.add(lat);
                           logitude.add(log);
                       }
                    }

                    else if(stat.equals("offline")){
                        for(int p = 0;p<playerEmail.size();p++){
                            if (email.equals(playerEmail.get(p))) {

                                playerEmail.remove(p);
                                playerName.remove(p);
                                latitude.remove(p);
                                logitude.remove(p);

                            }
                        }
                    }

                }
                Log.d("NearByPLayer/add11", playerEmail.toString());
                loadMap();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeAll() {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mGameDatabase = new GameDatabase();
        playerEmail = new ArrayList<>();
        playerName = new ArrayList<>();
        latitude = new ArrayList<>();
        logitude = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

}
