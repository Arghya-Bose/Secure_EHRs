package com.example.finalyearprojecta.viewprofile;

import com.google.firebase.Timestamp;

public class ProfileViewModel {

    private String viewerUid;
    private String viewerName;
    private String viewerRole;
    private String vContact;
    private Timestamp timestamp;

    public ProfileViewModel() {}

    public ProfileViewModel(String viewerUid,
                            String viewerName,
                            String viewerRole,
                            String vContact,
                            Timestamp timestamp) {
        this.viewerUid = viewerUid;
        this.viewerName = viewerName;
        this.viewerRole = viewerRole;
        this.vContact = vContact;
        this.timestamp = timestamp;
    }

    public String getViewerUid() { return viewerUid; }
    public String getViewerName() { return viewerName; }
    public String getViewerRole() { return viewerRole; }
    public Timestamp getTimestamp() { return timestamp; }
    public  String getvContact(){return vContact;}
}
