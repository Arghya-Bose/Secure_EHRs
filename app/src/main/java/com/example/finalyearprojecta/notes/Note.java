package com.example.finalyearprojecta.notes;

public class Note {
    String diagnosis, treatment, followUpDate;

    public Note() {}

    public Note(String diagnosis, String treatment, String followUpDate) {
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.followUpDate = followUpDate;
    }

    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }
    public String getFollowUpDate() { return followUpDate; }
}
