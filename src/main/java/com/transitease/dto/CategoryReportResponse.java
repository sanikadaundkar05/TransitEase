package com.transitease.dto;

public class CategoryReportResponse {

    private Long categoryId;
    private String categoryName;
    private Double baseFare;
    private Integer validityDays;
    private long totalApplications;
    private long approvedApplications;
    private long activePasses;
    private double totalEstimatedRevenue;

    public CategoryReportResponse() {
    }

    public CategoryReportResponse(Long categoryId, String categoryName, Double baseFare, Integer validityDays,
                                  long totalApplications, long approvedApplications, long activePasses,
                                  double totalEstimatedRevenue) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.baseFare = baseFare;
        this.validityDays = validityDays;
        this.totalApplications = totalApplications;
        this.approvedApplications = approvedApplications;
        this.activePasses = activePasses;
        this.totalEstimatedRevenue = totalEstimatedRevenue;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getApprovedApplications() {
        return approvedApplications;
    }

    public void setApprovedApplications(long approvedApplications) {
        this.approvedApplications = approvedApplications;
    }

    public long getActivePasses() {
        return activePasses;
    }

    public void setActivePasses(long activePasses) {
        this.activePasses = activePasses;
    }

    public double getTotalEstimatedRevenue() {
        return totalEstimatedRevenue;
    }

    public void setTotalEstimatedRevenue(double totalEstimatedRevenue) {
        this.totalEstimatedRevenue = totalEstimatedRevenue;
    }
}
