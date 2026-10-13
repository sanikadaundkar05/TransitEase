package com.transitease.dto;

import com.transitease.entity.ApplicationDocument;
import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private Long applicationId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private String documentType;
    private String status;
    private String remarks;
    private LocalDateTime uploadDate;

    public DocumentResponse() {
    }

    public static DocumentResponse fromEntity(ApplicationDocument doc) {
        if (doc == null) {
            return null;
        }
        DocumentResponse resp = new DocumentResponse();
        resp.setId(doc.getId());
        if (doc.getApplication() != null) {
            resp.setApplicationId(doc.getApplication().getId());
        }
        resp.setFileName(doc.getFileName());
        resp.setContentType(doc.getContentType());
        resp.setFileSize(doc.getFileSize());
        resp.setDownloadUrl("/api/documents/" + doc.getId() + "/download");
        resp.setDocumentType(doc.getDocumentType());
        resp.setStatus(doc.getStatus() != null ? doc.getStatus() : "PENDING");
        resp.setRemarks(doc.getRemarks());
        resp.setUploadDate(doc.getUploadDate());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
