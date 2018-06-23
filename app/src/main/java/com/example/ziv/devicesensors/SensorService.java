package com.example.ziv.devicesensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class SensorService extends Service {
    public SensorService() {}

    private final Random mGenerator = new Random();
    protected final IBinder sensorServiceBinder = new SensorServiceBinder();
    //sensorEventListener sensorEvtListener;
    SensorManager sensorManager;
    Sensor sensor;
    boolean checkSensor = true;
    protected float[] evaluate() {
        return new float[]{0.1f, 0.1f, 0.1f};
    }
    public SensorService getSelf() {return this;}

    @Override
    public IBinder onBind(Intent intent) {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        /*sensorEvtListener = new sensorEventListener();
        sensorManager.registerListener(sensorEvtListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
*/
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                while(checkSensor)
                {
                    handler.postDelayed(this, 1000);
                   /* getSample();
                    saveToDb();*/

                   Log.d("service", "run");

                }
            }
        };
        runnable.run();
        return sensorServiceBinder;


        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (sensorManager != null) {
            //sensorManager.unregisterListener(sensorEvtListener);
            sensorManager = null;
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        //sensorManager.unregisterListener(sensorEvtListener, sensor);
        super.onDestroy();
    }

/*    class sensorEventListener implements android.hardware.SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (checkSensor) {
                checkSensor = false;

               *//* float[] values = new float[event.values.length];
                for (int i = 0; i < event.values.length; i++) {
                    values[i] = event.values[i];// * 1000000.0f;
                }

                notifyEvaluation(values);*//*
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }*/

    class SensorServiceBinder extends Binder {
        SensorService getService()
        {
            return SensorService.this.getSelf();
        }
    }
}
