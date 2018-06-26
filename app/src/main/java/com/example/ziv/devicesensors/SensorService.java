package com.example.ziv.devicesensors;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Objects;

import io.realm.Realm;
import io.realm.Sort;


public class SensorService extends IntentService {

    SensorManager sensorManager;
    Sensor sensor;
    sensorEventListener sensorEvtListener;
    Realm realm;
    boolean checkSensor = true;
    private static final int FROM_RADS_TO_DEGS = -57;
    int yaw;
    int pitch;
    int roll;
    int countSample;

    //private final IBinder mBinder = new ServiceBinder();

    public SensorService() {
        super("sensor_service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensorEvtListener = new sensorEventListener();
            sensorManager.registerListener(sensorEvtListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        realm = Realm.getDefaultInstance();
        countSample =  realm.where(SensorSample.class).findAll().size();
        Log.d("service", "size1 " + countSample);

       /* while (checkSensor) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getSample();
        }*/

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (checkSensor) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getSample();
                }
            }
        };
        runnable.run();
    }


    private void getSample() {

        Log.d("service", "size2 " + countSample);
        if(countSample > 500)
        {
            removeOldest();
        }
        saveToDb();
    }

    private void saveToDb()
    {
        realm = Realm.getDefaultInstance();
        SensorSample sensorSample = new SensorSample();
        sensorSample.setDate();
        sensorSample.setYaw(yaw);
        sensorSample.setPitch(pitch);
        sensorSample.setRoll(roll);

        realm.beginTransaction();
        realm.copyToRealm(sensorSample);
        realm.commitTransaction();
        countSample++;

    }

    private void removeOldest()
    {
        realm.beginTransaction();
        SensorSample sensorSample = realm.where(SensorSample.class).sort("date", Sort.ASCENDING).findFirst();
        Objects.requireNonNull(sensorSample).deleteFromRealm();
        realm.commitTransaction();
        countSample--;
        //Log.d("service", "size3 " + countSample);
    }

    class sensorEventListener implements android.hardware.SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor == sensor) {
                if (event.values.length > 4) {
                    float[] truncatedRotationVector = new float[4];
                    System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                    update(truncatedRotationVector);
                }
                else {
                    update(event.values);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        private void update(float[] vectors) {

            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
            int worldAxisX = SensorManager.AXIS_X;
            int worldAxisZ = SensorManager.AXIS_Z;
            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);
            yaw = Math.round(orientation[0] * FROM_RADS_TO_DEGS);
            pitch = Math.round(orientation[1] * FROM_RADS_TO_DEGS);
            roll = Math.round(orientation[2] * FROM_RADS_TO_DEGS);

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkSensor = false;
    }

  /*  @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return mBinder;
    }

    public class ServiceBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensorEvtListener = new sensorEventListener();
            sensorManager.registerListener(sensorEvtListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        realm = Realm.getDefaultInstance();
        countSample =  realm.where(SensorSample.class).findAll().size();
        Log.d("service", "size1 " + countSample);

       *//* while (checkSensor) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getSample();
        }*//*

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (checkSensor) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getSample();
                }
            }
        };

        runnable.run();


        return START_STICKY;
    }*/
}