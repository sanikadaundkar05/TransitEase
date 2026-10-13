package com.transitease.dto;

import java.util.List;

public class PassengerHistoryResponse {

    private List<PassApplicationResponse> applications;
    private List<DigitalPassResponse> passes;

    public PassengerHistoryResponse() {
    }

    public PassengerHistoryResponse(List<PassApplicationResponse> applications, List<DigitalPassResponse> passes) {
        this.applications = applications;
        this.passes = passes;
    }

    public List<PassApplicationResponse> getApplications() {
        return applications;
    }

    public void setApplications(List<PassApplicationResponse> applications) {
        this.applications = applications;
    }

    public List<DigitalPassResponse> getPasses() {
        return passes;
    }

    public void setPasses(List<DigitalPassResponse> passes) {
        this.passes = passes;
    }
}
