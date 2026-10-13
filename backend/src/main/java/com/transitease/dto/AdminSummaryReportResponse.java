package com.transitease.dto;

public class AdminSummaryReportResponse {

    private long totalUsers;
    private long totalPassengers;
    private long totalOfficers;
    private long totalAdmins;

    private long totalApplications;
    private long pendingApplications;
    private long verifiedApplications;
    private long approvedApplications;
    private long rejectedApplications;
    private long renewalApplications;

    private long totalDigitalPasses;
    private long activeDigitalPasses;
    private long expiredDigitalPasses;
    private long suspendedDigitalPasses;

    private long totalRoutes;
    private long activeRoutes;
    private long totalPassCategories;

    public AdminSummaryReportResponse() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(long totalPassengers) {
        this.totalPassengers = totalPassengers;
    }

    public long getTotalOfficers() {
        return totalOfficers;
    }

    public void setTotalOfficers(long totalOfficers) {
        this.totalOfficers = totalOfficers;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(long pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public long getVerifiedApplications() {
        return verifiedApplications;
    }

    public void setVerifiedApplications(long verifiedApplications) {
        this.verifiedApplications = verifiedApplications;
    }

    public long getApprovedApplications() {
        return approvedApplications;
    }

    public void setApprovedApplications(long approvedApplications) {
        this.approvedApplications = approvedApplications;
    }

    public long getRejectedApplications() {
        return rejectedApplications;
    }

    public void setRejectedApplications(long rejectedApplications) {
        this.rejectedApplications = rejectedApplications;
    }

    public long getRenewalApplications() {
        return renewalApplications;
    }

    public void setRenewalApplications(long renewalApplications) {
        this.renewalApplications = renewalApplications;
    }

    public long getTotalDigitalPasses() {
        return totalDigitalPasses;
    }

    public void setTotalDigitalPasses(long totalDigitalPasses) {
        this.totalDigitalPasses = totalDigitalPasses;
    }

    public long getActiveDigitalPasses() {
        return activeDigitalPasses;
    }

    public void setActiveDigitalPasses(long activeDigitalPasses) {
        this.activeDigitalPasses = activeDigitalPasses;
    }

    public long getExpiredDigitalPasses() {
        return expiredDigitalPasses;
    }

    public void setExpiredDigitalPasses(long expiredDigitalPasses) {
        this.expiredDigitalPasses = expiredDigitalPasses;
    }

    public long getSuspendedDigitalPasses() {
        return suspendedDigitalPasses;
    }

    public void setSuspendedDigitalPasses(long suspendedDigitalPasses) {
        this.suspendedDigitalPasses = suspendedDigitalPasses;
    }

    public long getTotalRoutes() {
        return totalRoutes;
    }

    public void setTotalRoutes(long totalRoutes) {
        this.totalRoutes = totalRoutes;
    }

    public long getActiveRoutes() {
        return activeRoutes;
    }

    public void setActiveRoutes(long activeRoutes) {
        this.activeRoutes = activeRoutes;
    }

    public long getTotalPassCategories() {
        return totalPassCategories;
    }

    public void setTotalPassCategories(long totalPassCategories) {
        this.totalPassCategories = totalPassCategories;
    }
}
