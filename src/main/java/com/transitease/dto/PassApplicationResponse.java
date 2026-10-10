package com.transitease.dto;

import com.transitease.entity.PassApplication;
import java.time.LocalDateTime;

public class PassApplicationResponse {
    
    private Long id;
    private Long passCategoryId;
    private String passCategoryName;
    private String passengerName;
    private String passengerEmail;
    private String documentUrl;
    private String status;
    private LocalDateTime applicationDate;
    
    public PassApplicationResponse() {}
    
    public static PassApplicationResponse fromEntity(PassApplication application) {
        PassApplicationResponse resp = new PassApplicationResponse();
        resp.setId(application.getId());
        resp.setPassCategoryId(application.getPassCategory().getId());
        resp.setPassCategoryName(application.getPassCategory().getName());
        resp.setPassengerName(application.getPassenger().getFullName());
        resp.setPassengerEmail(application.getPassenger().getEmail());
        resp.setDocumentUrl(application.getDocumentUrl());
        resp.setStatus(application.getStatus().name());
        resp.setApplicationDate(application.getApplicationDate());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPassCategoryId() {
        return passCategoryId;
    }

    public void setPassCategoryId(Long passCategoryId) {
        this.passCategoryId = passCategoryId;
    }

    public String getPassCategoryName() {
        return passCategoryName;
    }

    public void setPassCategoryName(String passCategoryName) {
        this.passCategoryName = passCategoryName;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
}
