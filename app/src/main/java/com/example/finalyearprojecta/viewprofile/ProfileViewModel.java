package com.example.finalyearprojecta.viewprofile;

import com.google.firebase.Timestamp;

public class ProfileViewModel {

    private String viewerUid;
    private String viewerName;
    private String viewerRole;
    private Timestamp timestamp;

    public ProfileViewModel() {}

    public ProfileViewModel(String viewerUid,
                            String viewerName,
                            String viewerRole,
                            Timestamp timestamp) {
        this.viewerUid = viewerUid;
        this.viewerName = viewerName;
        this.viewerRole = viewerRole;
        this.timestamp = timestamp;
    }

    public String getViewerUid() { return viewerUid; }
    public String getViewerName() { return viewerName; }
    public String getViewerRole() { return viewerRole; }
    public Timestamp getTimestamp() { return timestamp; }
}
