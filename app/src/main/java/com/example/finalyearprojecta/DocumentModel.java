package com.example.finalyearprojecta;

public class DocumentModel {

    String fileName;
    String uploadedBy;
    String role;
    String fileData;
    String feedback;
    String uploadDate;
    String fileType;

    private String category;
    private String subCategory;

    // 🔥 FULL CONSTRUCTOR (NEW)
    public DocumentModel(String fileName, String uploadedBy, String role,
                         String fileData, String feedback, String uploadDate,
                         String fileType,
                         String category, String subCategory) {

        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.role = role;
        this.fileData = fileData;
        this.feedback = feedback;
        this.uploadDate = uploadDate;
        this.fileType = fileType;
        this.category = category;
        this.subCategory = subCategory;
    }

    // 🔥 OLD CONSTRUCTOR (for your existing report code)
    public DocumentModel(String fileName, String uploadedBy, String role,
                         String fileData, String feedback, String uploadDate,
                         String category, String subCategory) {

        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
        this.role = role;
        this.fileData = fileData;
        this.feedback = feedback;
        this.uploadDate = uploadDate;
        this.category = category;
        this.subCategory = subCategory;

        // default fallback
        this.fileType = "pdf";
    }

    // ================= GETTERS =================

    public String getFileName() { return fileName; }

    public String getUploadedBy() { return uploadedBy; }

    public String getRole() { return role; }

    public String getFileData() { return fileData; }

    public String getFeedback() { return feedback; }

    public String getUploadDate() { return uploadDate; }

    public String getCategory() { return category; }

    public String getSubCategory() { return subCategory; }

    public String getFileType() { return fileType; }
}