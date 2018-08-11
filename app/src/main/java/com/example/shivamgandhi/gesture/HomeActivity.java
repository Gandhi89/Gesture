package com.example.shivamgandhi.gesture;

import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

import java.util.Random;

public class HomeActivity extends AppCompatActivity implements ShakeDetector.Listener, View.OnClickListener {

    private int shakeCount = 0;
    ImageView iv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        iv = findViewById(R.id.homeActivity_imageView);
        btn = findViewById(R.id.homeActivity_button);
        btn.setVisibility(View.INVISIBLE);

        btn.setOnClickListener(this);

    }

    /**
     * This method will be called after every shake.
     */
    public void hearShake() {
        shakeCount ++;
        /**
         * For shake count 1 to 5
         */
        if (shakeCount < 6) {
            Toast.makeText(this, "Shake Count :- " + shakeCount, Toast.LENGTH_SHORT).show();
        }
        /**
         *  For shake count equals 6
         */
        else if(shakeCount == 6)
        {
            Random rm = new Random();
            int numberGenerated = rm.nextInt(3);
            if (numberGenerated == 0)
            {
                iv.setImageResource(R.drawable.rock);
                btn.setVisibility(View.VISIBLE);
            }
            else if (numberGenerated == 1)
            {
                iv.setImageResource(R.drawable.paper);
                btn.setVisibility(View.VISIBLE);
            }
            else if (numberGenerated == 2)
            {
                iv.setImageResource(R.drawable.scissors);
                btn.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
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

