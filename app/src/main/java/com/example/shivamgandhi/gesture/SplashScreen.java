package com.example.shivamgandhi.gesture;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {


    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    GameDatabase mGameDatabase;
    Vars mVars;
    User mUser;
    ArrayList<String> userPrimaryKeys;
    ArrayList<String> emails ;

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


        final Intent myintent = new Intent(this, MainActivity.class);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                startActivity(myintent);
                finish();
            }
        }, 4000);
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
        mVars.setRegisteredUsers(emails);
        mVars.setUserPrimaryKey(userPrimaryKeys);
    }
}
