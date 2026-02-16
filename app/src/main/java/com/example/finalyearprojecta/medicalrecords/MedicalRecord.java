package com.example.finalyearprojecta.medicalrecords;

public class MedicalRecord {

    String type, title, year, notes;

    public MedicalRecord(String type, String title, String year, String notes) {
        this.type = type;
        this.title = title;
        this.year = year;
        this.notes = notes;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getNotes() { return notes; }
}
