package com.transitease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PassApplicationRequest {
    
    @NotNull(message = "Pass category ID is required")
    private Long passCategoryId;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;

    public PassApplicationRequest() {
    }

    public PassApplicationRequest(Long passCategoryId, String documentUrl) {
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
