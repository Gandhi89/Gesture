package com.example.shivamgandhi.gesture;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    Vars mVars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        iv = findViewById(R.id.homeActivity_imageView);
        timer = findViewById(R.id.homeActivity_timer);
        btn = findViewById(R.id.homeActivity_button);
        btn.setVisibility(View.INVISIBLE);

        btn.setOnClickListener(this);
        mGameDatabase = new GameDatabase();
        mVars = Vars.getInstance();

        new CountDownTimer(10000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                timer.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                timer.setText("done!");
                
            }
        }.start();
    }

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
                mGameDatabase.setRPSvalue(mVars.getPlayerName(),"rock");
                //btn.setVisibility(View.VISIBLE);
            } else if (numberGenerated == 1) {
                iv.setImageResource(R.drawable.paper);
                mGameDatabase.setRPSvalue(mVars.getPlayerName(),"paper");
                //btn.setVisibility(View.VISIBLE);
            } else if (numberGenerated == 2) {
                iv.setImageResource(R.drawable.scissors);
                mGameDatabase.setRPSvalue(mVars.getPlayerName(),"scissors");
                //btn.setVisibility(View.VISIBLE);
            }

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
}

