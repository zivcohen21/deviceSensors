package com.example.ziv.devicesensors;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

import io.realm.Realm;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    Realm realm;
    boolean checkSensor = true;
    Intent intent;
    private SensorService sServ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //changeBackground();
        ServiceThread s = new ServiceThread(intent);
        new Thread(s).start();

        MainThread m = new MainThread(realm, checkSensor);
        new Thread(m).start();
    }


    class ServiceThread implements Runnable {

        Intent intent;
        ServiceThread(Intent intent) {
            this.intent = intent;
        }

        public void run() {
            Log.d("main", "ServiceThread");
            //doBindService();
            this.intent = new Intent(MainActivity.this, SensorService.class);
            startService(this.intent);
        }
    }

    class MainThread implements Runnable {

        Realm realm;
        boolean checkSensor;
        MainThread(Realm realm, boolean checkSensor) {
            this.realm = realm;
            this.checkSensor = checkSensor;
        }

        public void run() {
            Log.d("main", "MainThread");
            this.realm = Realm.getDefaultInstance();
            changeBackground(this.realm, this.checkSensor);
        }
    }

    protected void changeBackground(Realm realm, boolean checkSensor)
    {
        while(checkSensor)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            realm.beginTransaction();
            SensorSample sensorSample = realm.where(SensorSample.class).sort("date", Sort.DESCENDING).findFirst();
            realm.commitTransaction();
            Log.d("main", "sensorSample to show " + Objects.requireNonNull(sensorSample).getId());
            Log.d("main", "pitch: " + Objects.requireNonNull(sensorSample).getPitch());
            if(sensorSample.getPitch() > 70 && sensorSample.getPitch() < 180)
            {
                Log.d("main", "Down");
            }
            else if(sensorSample.getPitch() > -60 && sensorSample.getPitch() < 70)
            {
                Log.d("main", "User");
            }
            else if(sensorSample.getPitch() > -180 && sensorSample.getPitch() < -60)
            {
                Log.d("main", "Down");
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();



    }

    @Override
    protected void onStop() {
        super.onStop();
        checkSensor = false;
    }

 /*   //music manager function
    private ServiceConnection Scon =new ServiceConnection()
    {
        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            sServ = ((SensorService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            sServ = null;
        }
    };

    void doBindService()
    {
        bindService(new Intent(this,SensorService.class),
                Scon, Context.BIND_AUTO_CREATE);
    }
*/
}

