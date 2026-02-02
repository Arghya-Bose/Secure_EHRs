package com.example.finalyearprojecta;

public class DocumentModel {
    private String fileName;
    private String uploadedBy;
    private String role;
    private String fileData; // Base64 string

    public DocumentModel(String fileName, String uploadedBy, String role, String fileData){
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.role = role;
        this.fileData = fileData;
    }

    public String getFileName(){ return fileName; }
    public String getUploadedBy(){ return uploadedBy; }
    public String getRole(){ return role; }
    public String getFileData(){ return fileData; }
}
