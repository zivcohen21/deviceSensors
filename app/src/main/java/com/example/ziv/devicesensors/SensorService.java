package com.example.ziv.devicesensors;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Objects;

import io.realm.Realm;


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
    int index;
    int oldestSampleIndex;
    int countSample;

    public SensorService() {
        super("");
    }
    public SensorService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensorEvtListener = new sensorEventListener();
            sensorManager.registerListener(sensorEvtListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        countSample = 0;
        index = 1;
        oldestSampleIndex = 1;
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        Runnable runnable = new Runnable() {
        Thread thread;
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

        if(countSample > 500)
        {
            removeOldest();
        }
        saveToDb();
    }

    private void saveToDb()
    {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                SensorSample sensorSample = realm.createObject(SensorSample.class);
                sensorSample.setId(index++);
                sensorSample.setDate();
                sensorSample.setYaw(yaw);
                sensorSample.setPitch(pitch);
                sensorSample.setRoll(roll);
                countSample++;
            }
        });
    }

    private void removeOldest()
    {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Objects.requireNonNull(realm.where(SensorSample.class)
                        .equalTo("id", oldestSampleIndex).findFirst()).deleteFromRealm();
                countSample--;
            }
        });
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
}