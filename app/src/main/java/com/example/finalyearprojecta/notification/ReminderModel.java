package com.example.finalyearprojecta.notification;

import java.io.Serializable;

public class ReminderModel implements Serializable {

    private String medicineName;
    private String time;
    private int requestCode;

    public ReminderModel(String medicineName, String time, int requestCode) {
        this.medicineName = medicineName;
        this.time = time;
        this.requestCode = requestCode;
    }

    public String getMedicineName() { return medicineName; }
    public String getTime()         { return time; }
    public int getRequestCode()     { return requestCode; }
}