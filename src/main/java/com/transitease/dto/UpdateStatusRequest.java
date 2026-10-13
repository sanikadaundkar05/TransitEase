package com.transitease.dto;

import com.transitease.entity.ApplicationStatus;

public class UpdateStatusRequest {

    private ApplicationStatus status;
    private String remarks;
    private String rejectionReason;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(ApplicationStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks != null ? remarks : rejectionReason;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRejectionReason() {
        return rejectionReason != null ? rejectionReason : remarks;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
