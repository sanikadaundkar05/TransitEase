package com.transitease.dto;

public class PassRenewalRequest {

    private Long passCategoryId;
    private String documentUrl;

    public PassRenewalRequest() {
    }

    public PassRenewalRequest(Long passCategoryId, String documentUrl) {
        this.passCategoryId = passCategoryId;
        this.documentUrl = documentUrl;
    }

    public Long getPassCategoryId() {
        return passCategoryId;
    }

    public void setPassCategoryId(Long passCategoryId) {
        this.passCategoryId = passCategoryId;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }
}
