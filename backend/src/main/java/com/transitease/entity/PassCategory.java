package com.transitease.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pass_categories")
public class PassCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer validityDays;

    @Column(nullable = false)
    private Double baseFare;

    @Column(length = 50)
    private String categoryGroup; // GENERAL, STUDENT, SENIOR_CITIZEN, CONCESSIONARY

    @Column(length = 1000)
    private String eligibility;

    @Column(length = 1000)
    private String mandatoryDocuments;

    @Column(length = 1000)
    private String optionalDocuments;

    @Column(length = 50)
    private String fareType; // FIXED, CONFIRMATION_REQUIRED, CONFIGURABLE, CONCESSIONAL

    @Column(length = 1000)
    private String discountDetails;

    public PassCategory() {
    }

    public PassCategory(Long id, String name, String description, Integer validityDays, Double baseFare) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.validityDays = validityDays;
        this.baseFare = baseFare;
    }

    public PassCategory(Long id, String name, String description, Integer validityDays, Double baseFare,
                        String categoryGroup, String eligibility, String mandatoryDocuments,
                        String optionalDocuments, String fareType, String discountDetails) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
