package com.example.ziv.devicesensors;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.Objects;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


public class SensorService extends IntentService {

    final int MAX_SAMPLES = 500;
    final int FROM_RADS_TO_DEGS = -57;
    SensorManager sensorManager;
    Sensor sensor;
    sensorEventListener sensorEvtListener;
    Realm realm;
    RealmResults<SensorSample> samples;
    boolean checkSensor = true;
    int yaw;
    int pitch;
    int roll;
    int countSample;

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
        else {
            Log.d("service", "SensorManager null");
        }
        realm = Realm.getDefaultInstance();
        samples = realm.where(SensorSample.class).findAll();

        realm.beginTransaction();
        samples.deleteAllFromRealm();
        realm.commitTransaction();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (checkSensor) {
                    try {
                        Thread.sleep(300);
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
        if(countSample >= MAX_SAMPLES)
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
        Log.d("service", "pitch " + pitch);
    }

    private void removeOldest()
    {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        SensorSample sensorSample = realm.where(SensorSample.class).sort("date", Sort.ASCENDING).findFirst();
        Objects.requireNonNull(sensorSample).deleteFromRealm();
        realm.commitTransaction();
        countSample--;
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

 }