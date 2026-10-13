package com.transitease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class PassCategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Validity days is required")
    @Positive(message = "Validity days must be positive")
    private Integer validityDays;

    @NotNull(message = "Base fare is required")
    @PositiveOrZero(message = "Base fare must be non-negative")
    private Double baseFare = 0.0;

    private String categoryGroup;
    private String eligibility;
    private String mandatoryDocuments;
    private String optionalDocuments;
    private String fareType;
    private String discountDetails;

    public PassCategoryRequest() {
    }

    public PassCategoryRequest(String name, String description, Integer validityDays, Double baseFare) {
        this.name = name;
        this.description = description;
        this.validityDays = validityDays;
        this.baseFare = baseFare;
    }

    public PassCategoryRequest(String name, String description, Integer validityDays, Double baseFare,
                               String categoryGroup, String eligibility, String mandatoryDocuments,
                               String optionalDocuments, String fareType, String discountDetails) {
        this.name = name;
        this.description = description;
        this.validityDays = validityDays;
        this.baseFare = baseFare;
        this.categoryGroup = categoryGroup;
        this.eligibility = eligibility;
        this.mandatoryDocuments = mandatoryDocuments;
        this.optionalDocuments = optionalDocuments;
        this.fareType = fareType;
        this.discountDetails = discountDetails;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public String getCategoryGroup() {
        return categoryGroup;
    }

    public void setCategoryGroup(String categoryGroup) {
        this.categoryGroup = categoryGroup;
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public String getMandatoryDocuments() {
        return mandatoryDocuments;
    }

    public void setMandatoryDocuments(String mandatoryDocuments) {
        this.mandatoryDocuments = mandatoryDocuments;
    }

    public String getOptionalDocuments() {
        return optionalDocuments;
    }

    public void setOptionalDocuments(String optionalDocuments) {
        this.optionalDocuments = optionalDocuments;
    }

    public String getFareType() {
        return fareType;
    }

    public void setFareType(String fareType) {
        this.fareType = fareType;
    }

    public String getDiscountDetails() {
        return discountDetails;
    }

    public void setDiscountDetails(String discountDetails) {
        this.discountDetails = discountDetails;
    }
}
