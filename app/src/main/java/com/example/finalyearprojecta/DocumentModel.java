package com.example.finalyearprojecta;

public class DocumentModel {

    String fileName;
    String uploadedBy;
    String role;
    String fileData;
    String feedback;

    public DocumentModel(String fileName, String uploadedBy, String role,
                         String fileData, String feedback) {
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.role = role;
        this.fileData = fileData;
        this.feedback = feedback;
    }

    public String getFileName() { return fileName; }
    public String getUploadedBy() { return uploadedBy; }
    public String getRole() { return role; }
    public String getFileData() { return fileData; }
    public String getFeedback() { return feedback; }
}
