package com.example.ziv.devicesensors;

import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import io.realm.Realm;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    boolean checkSensor = true;
    Intent intent;
    ConstraintLayout mainScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainScreen = (ConstraintLayout) this.findViewById(R.id.main_screen);
        intent = new Intent(MainActivity.this, SensorService.class);

        ServiceThread s = new ServiceThread(intent);
        s.start();

        MainThread m = new MainThread(mainScreen);
        m.start();
    }

    class ServiceThread extends Thread {

        Intent intent;
        ServiceThread(Intent intent) {
            this.intent = intent;
        }

        public void run() {
            startService(this.intent);
        }
    }

    class MainThread extends Thread {

        ConstraintLayout mainScreen;
        MainThread(ConstraintLayout mainScreen) {
            this.mainScreen =  mainScreen;
        }

        public void run() {
            changeBackground(this.mainScreen);
        }
    }

    protected void changeBackground(final ConstraintLayout mainScreen)
    {
        Realm realm = Realm.getDefaultInstance();
        while(checkSensor)
        {
            realm.beginTransaction();
            final SensorSample sensorSample = realm.where(SensorSample.class).sort("date", Sort.DESCENDING).findFirst();
            realm.commitTransaction();
            final int pitch = sensorSample != null ? sensorSample.getPitch() : 0;
            Log.d("main", "pitch: " + pitch);

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    paintScreen(mainScreen, pitch);
                }
            });
        }
    }

    private void paintScreen(ConstraintLayout mainScreen, int pitch) {
        if(pitch > 45 && pitch < 195)
        {
            Log.d("main", "Down");
            mainScreen.setBackgroundColor(Color.RED);
        }
        else if(pitch > -45 && pitch < 45)
        {
            Log.d("main", "User");
            mainScreen.setBackgroundColor(Color.GREEN);
        }
        else if(pitch > -195 && pitch < -45)
        {
            Log.d("main", "Up");
            mainScreen.setBackgroundColor(Color.BLUE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkSensor = false;
    }
}



