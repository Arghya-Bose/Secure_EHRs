package com.example.finalyearprojecta;

public class DocumentModel {

    String fileName;
    String uploadedBy;
    String role;
    String fileData;
    String feedback;
    String uploadDate;

    public DocumentModel(String fileName, String uploadedBy, String role,
                         String fileData, String feedback, String uploadDate) {
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.role = role;
        this.fileData = fileData;
        this.feedback = feedback;
        this.uploadDate = uploadDate;
    }

    public String getFileName() { return fileName; }
    public String getUploadedBy() { return uploadedBy; }
    public String getRole() { return role; }
    public String getFileData() { return fileData; }
    public String getFeedback() { return feedback; }

    public String getUploadDate() {
        return uploadDate;
    }
}
