package com.example.ziv.devicesensors;

import android.util.Log;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;

public class SensorSample extends RealmObject {

    private String id;
    private long date;
    private int yaw;
    private int pitch;
    private int roll;

    public SensorSample()
    {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate() {
        Date now = new Date();
        this.date = now.getTime();
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }
}
