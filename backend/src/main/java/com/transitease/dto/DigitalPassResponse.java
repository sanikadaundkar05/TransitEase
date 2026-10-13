package com.transitease.dto;

import com.transitease.entity.DigitalPass;
import java.time.LocalDate;

public class DigitalPassResponse {

    private Long id;
    private String passNumber;
    private Long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Long applicationId;
    private Long passCategoryId;
    private String passCategoryName;
    private LocalDate issueDate;
    private LocalDate validityStartDate;
    private LocalDate validityEndDate;
    private String status;

    public DigitalPassResponse() {
    }

    public static DigitalPassResponse fromEntity(DigitalPass pass) {
        if (pass == null) {
            return null;
        }
        DigitalPassResponse resp = new DigitalPassResponse();
        resp.setId(pass.getId());
        resp.setPassNumber(pass.getPassNumber());

        if (pass.getPassenger() != null) {
            resp.setPassengerId(pass.getPassenger().getId());
            resp.setPassengerName(pass.getPassenger().getFullName());
            resp.setPassengerEmail(pass.getPassenger().getEmail());
        }

        if (pass.getApplication() != null) {
            resp.setApplicationId(pass.getApplication().getId());
            if (pass.getApplication().getPassCategory() != null) {
                resp.setPassCategoryId(pass.getApplication().getPassCategory().getId());
                resp.setPassCategoryName(pass.getApplication().getPassCategory().getName());
            }
        }

        resp.setIssueDate(pass.getIssueDate());
        resp.setValidityStartDate(pass.getValidityStartDate());
        resp.setValidityEndDate(pass.getValidityEndDate());
        resp.setStatus(pass.getStatus() != null ? pass.getStatus().name() : null);

        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassNumber() {
        return passNumber;
    }

    public void setPassNumber(String passNumber) {
        this.passNumber = passNumber;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
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

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(LocalDate validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

    public LocalDate getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate(LocalDate validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
