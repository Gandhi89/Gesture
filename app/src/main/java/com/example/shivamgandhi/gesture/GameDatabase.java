package com.example.shivamgandhi.gesture;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class GameDatabase {

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    Vars mVars;
    Player mPlayer;
    int point_r = 0;
    int point_p = 0;
    int point_s = 0;
    int count_r = 0, count_p = 0, count_s = 0, count_none = 0;
    ArrayList<String> winingStatus = new ArrayList<>(); // IN ORDER OF RPS
    String getWiningStatus_R, getWiningStatus_P, getWiningStatus_S;
    String stat, gameStatus = "status";
    String playerStatus = "status";
    User mUser;
    private Handler mHandler = new Handler();
    ArrayList<String> userChoice = new ArrayList<>(); // STORE RPS OF EACH PLAYER
    ArrayList<String> winingIDs = new ArrayList<>();
    ArrayList<String> playerNames = new ArrayList<>(); // ALL PLAYERNAMES IN DATABASE

    public void GameDatabase() {
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to create new game
     */
    public void createGame() {
        mVars = Vars.getInstance();
        mVars.setGameID(generateGameID());

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").setValue("waiting");
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players");

    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to create player and join into existing game
     */
    public String createPlayer(String playerName) {
        mVars = Vars.getInstance();
        mVars.setPlayerName(playerName);
        mPlayer = new Player(playerName, "default", "none", "not ready");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        DatabaseReference games = mDatabaseReference.child("game");
        DatabaseReference game = games.child(mVars.getGameID());
        DatabaseReference players = game.child("players");
        DatabaseReference player = players.push();
        player.setValue(mPlayer);

        return player.getKey();

    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to add Users
     */
    public void addUser(String email, String uName, int wining, String title, double lat, double log, String status) {
        mVars = Vars.getInstance();
        mVars.setGameID(generateGameID());
        mUser = new User();
        mUser = new User(email, uName, wining, title, lat, log, status);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mDatabaseReference.child("Users").push().setValue(mUser);

    }


    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to generate 6 digit long random number for unique gameid
     */
    public String generateGameID() {
        Random rnd = new Random();
        int number = 100000 + rnd.nextInt(900000);
        return String.valueOf(number);
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to change status of game
     */
    public void changeGameStatus(String status) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("status").setValue(status);

    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to set RPS value
     */
    public void setRPSvalue(String playerName, String value) {
        mVars = Vars.getInstance();
        mPlayer = new Player(playerName, "default", value, "not ready");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).setValue(mPlayer);
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to calculate which team wins
     */
    public ArrayList<String> calResult(int cnt_r, int cnt_p, int cnt_s, int cnt_none) {

        Log.d("val/cnt_r: ", "" + cnt_r);
        Log.d("val/cnt_p: ", "" + cnt_p);
        Log.d("val/cnt_s: ", "" + cnt_s);
        Log.d("val/cnt_r: ", "" + cnt_none);

        /**
         * get count of R/P/S -> count points of team R/P/S
         */
        point_r = cnt_s;
        point_p = cnt_r;
        point_s = cnt_p;

        Log.d("val/point_r: ", "" + point_r);
        Log.d("val/point_p: ", "" + point_p);
        Log.d("val/point_s: ", "" + point_s);

        /**
         * get max point of team R/P/S
         */
        int max_point = point_p;
        if (max_point < point_r) {
            max_point = point_r;
        }
        if (max_point < point_s) {
            max_point = point_s;
        }

        Log.d("val/max_point: ", "" + max_point);

        /**
         * count how many teams have max_point score
         */
        int no_max_point = 0;
        if (point_r == max_point) {
            no_max_point++;
        }
        if (point_p == max_point) {
            no_max_point++;
        }
        if (point_s == max_point) {
            no_max_point++;
        }

        Log.d("val/no_max_point: ", "" + no_max_point);

        String winingStatus_rock = "lose";
        String winingStatus_paper = "lose";
        String winingStatus_scissors = "lose";

        /**
         * case - every player got same option [R/P/S]
         */
        // ------------------------------------ //
        if (max_point == (cnt_r + cnt_p + cnt_s + cnt_none)) {
            winingStatus_rock = "draw";
            winingStatus_paper = "draw";
            winingStatus_scissors = "draw";
            no_max_point = 4;
        }

        /**
         * case - one [team] definite winner
         */
        // ------------------------------------ //
        if (no_max_point == 1) {
            Log.d("val/inside-1: ", "" + 1);

            if (max_point == cnt_r) {
                winingStatus_rock = "win";
                Log.d("val/inside-1-R: ", "" + 1);
            }
            if (max_point == cnt_p) {
                winingStatus_paper = "win";
                Log.d("val/inside-1-P: ", "" + 1);
            }
            if (max_point == cnt_s) {
                winingStatus_scissors = "win";
                Log.d("val/inside-1-s: ", "" + 1);
            }
            if ((cnt_r > 0) && (winingStatus_scissors.equals("win"))) {
                winingStatus_scissors = "lose";
                winingStatus_rock = "win";
            } else if ((cnt_p > 0) && (winingStatus_rock.equals("win"))) {
                winingStatus_rock = "lose";
                winingStatus_paper = "win";
            } else if ((cnt_s > 0) && (winingStatus_paper.equals("win"))) {
                winingStatus_paper = "lose";
                winingStatus_scissors = "win";
            }
        }

        /**
         * case - two [team] possible winners
         */
        // ------------------------------------ //
        else if (no_max_point == 2) {
            Log.d("val/inside-2: ", "" + 2);

            if (point_r == max_point) {
                if (cnt_r > 0) {
                    winingStatus_rock = "win";
                }
            }
            if (point_p == max_point) {
                if (cnt_p > 0) {
                    winingStatus_paper = "win";
                }
            }
            if (point_s == max_point) {
                if (cnt_s > 0) {
                    winingStatus_scissors = "win";
                }
            }
        }

        /**
         * case - three [team] definite winners = draw
         */
        // ------------------------------------ //
        else if (no_max_point == 3) {
            Log.d("val/inside-3: ", "" + 3);
            if (cnt_none > 0) {
                winingStatus_scissors = "win";
                winingStatus_rock = "win";
                winingStatus_paper = "win";
            } else {
                Log.d("val/inside-3-draw: ", "" + 3);
                winingStatus_scissors = "draw";
                winingStatus_rock = "draw";
                winingStatus_paper = "draw";
            }
        }

        winingStatus.add(winingStatus_rock);
        winingStatus.add(winingStatus_paper);
        winingStatus.add(winingStatus_scissors);

        Log.d("val/Rock:- ", winingStatus_rock);
        Log.d("val/Paper:- ", winingStatus_paper);
        Log.d("val/Scissors:- ", winingStatus_scissors);
        return winingStatus;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to fetch RPS of every player
     */
    public ArrayList<String> getRPSValue() {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userChoice.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Player player = postSnapshot.getValue(Player.class);
                    userChoice.add(player.RPS);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("cal/userValue", "RPS value:- " + userChoice);
        return userChoice;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to calculate[set] count_r,count_p,count_s
     */
    public void setCount_rps(ArrayList<String> userChoice) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        this.userChoice = userChoice;

        for (int i = 0; i < this.userChoice.size(); i++) {
            if (this.userChoice.get(i).equals("rock")) {
                count_r++;
                Log.d("CalculateResult/cnt_r:-", "" + count_r);
            } else if (this.userChoice.get(i).equals("paper")) {
                count_p++;
                Log.d("CalculateResult/cnt_p:-", "" + count_p);
            } else if (this.userChoice.get(i).equals("scissors")) {
                count_s++;
                Log.d("CalculateResult/cnt_s:-", "" + count_s);
            } else {
                count_none++;
                Log.d("CalculateResult/cnt_n:-", "" + count_none);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function that returns[get] count of ROCK
     */
    public int getCount_r() {
        return count_r;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function that returns[get] count of PAPER
     */
    public int getCount_p() {
        return count_p;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function that returns[get] count of SCISSORS
     */
    public int getCount_s() {
        return count_s;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function that returns[get] count of SCISSORS
     */
    public int getCount_n() {
        return count_none;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to update playerStatus[win/lose] based on winingStatus_r,winingStatus_p,winingStatus_s
     */
    public void updatePlayerStatus(String getWiningStatus_r, String getWiningStatus_p, String getWiningStatus_s) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        getWiningStatus_R = getWiningStatus_r;
        getWiningStatus_P = getWiningStatus_p;
        getWiningStatus_S = getWiningStatus_s;

        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Player player = postSnapshot.getValue(Player.class);

                    /**
                     * change status[win/lose/none] of player in Player class
                     */
                    if (player.RPS.equals("rock")) {
                        player.status = getWiningStatus_R;
                        Log.d("Calcula/np/R", player.status);
                    } else if (player.RPS.equals("paper")) {
                        player.status = getWiningStatus_P;
                        Log.d("Calcula/np/P", player.status);
                    } else if (player.RPS.equals("scissors")) {
                        player.status = getWiningStatus_S;
                        Log.d("Calcula/np/S", player.status);
                    } else {
                        player.status = "lose";
                    }

                    String key = postSnapshot.getKey();
                    /**
                     * update the status of player in Firebase database
                     */
                    mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).setValue(player);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to fetchPlayerStatus AND to delete every "lose" child of game AND set playerStatus "champion" if derived
     */
    public String fetchPlayerStatus() {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**
                 * get number of players in game
                 */
                long count = dataSnapshot.getChildrenCount();
                Log.d("val/No_players:-", dataSnapshot.getChildrenCount() + "");
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String key = postSnapshot.getKey();
                    Player player = postSnapshot.getValue(Player.class);
                    stat = player.status;
                    Log.d("val/playerStat:-", stat);

                    if (stat.equals("win")) {
                        winingIDs.add(key);
                    }

                    if (key.equals(mVars.getPlayerID())) {

                        if (stat.equals("win")) {
                            Log.d("val/uuuStat:-", stat);
                            playerStatus = "win";
                        }
                        if (stat.equals("draw")) {
                            Log.d("val/uuuStat:-", stat);
                            playerStatus = "draw";
                        }
                        /**
                         * delete child if status == "lose"
                         */
                        if (stat.equals("lose")) {
                            Log.d("val/uuuStat:-", stat);
                            playerStatus = "lose";
                            Log.d("val/insidePhnPLa:-", "delete player plying from phn");
                            mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).removeValue();
                            count--;
                        }
                    } else {
                        /**
                         * delete child if status == "lose"
                         */
                        if (stat.equals("lose")) {
                            Log.d("val/insideFBPLa:-", "delete player plying from FB");
                            mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).removeValue();
                            count--;
                        }
                    }
                }
                /**
                 * if only one player left in game, change status to champion and change status of game to "done"
                 */
                Log.d("val/Count:-", "" + count);

                if (count == 1) {
                    Log.d("val/inside????:-", "commented part");
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String key = postSnapshot.getKey();

                        for (int y = 0; y < winingIDs.size(); y++) {
                            if (key.equals(winingIDs.get(y))) {
                                Player player = postSnapshot.getValue(Player.class);
                                player.status = "champion";
                                playerStatus = "champion";

                                mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(key).setValue(player);
                                changeGameStatus("done");
                            }
                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d("val/finalPlaySta", playerStatus);
        return playerStatus;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to reset RPS value[none]
     */
    public void RPSreset() {
        updatePlayerStatus("none", "none", "none");
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to set readyValue of player
     */
    public void updateReadyValue(String value) {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mPlayer = new Player(mVars.getPlayerName(), "default", "none", value);


        mDatabaseReference.child("game").child(mVars.getGameID()).child("players").child(mVars.getPlayerID()).setValue(mPlayer);

    }

    public void removePlayerFromGame() {
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to updateLocation of User
     */
    public void updateCurrentLocation(final double lat, final double log){
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        final String primaryKey = mVars.getPrimarykey();

        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postData:dataSnapshot.getChildren()){
                    if (primaryKey.equals(postData.getKey())){

                        User user = postData.getValue(User.class);
                        user.lat = lat;
                        user.log = log;

                        mDatabaseReference.child("Users").child(postData.getKey()).setValue(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * function to update status of user
     */
    public void updateUserStatus(final String status){
        mVars = Vars.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        final String primaryKey = mVars.getPrimarykey();

        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postData:dataSnapshot.getChildren()){
                    if (primaryKey.equals(postData.getKey())){

                        User user = postData.getValue(User.class);
                        user.Status = status;


                        mDatabaseReference.child("Users").child(postData.getKey()).setValue(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
