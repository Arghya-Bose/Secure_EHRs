package com.example.finalyearprojecta.appointment;
public class AppointmentModel {
    String id, doctorId, date, time, status, patientId;

    public AppointmentModel(String id, String doctorId, String date, String time, String status, String patientId) {
        this.id = id;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.patientId = patientId;
    }

    public String getId() { return id; }
    public String getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public String getPatientId() { return patientId; }
}
