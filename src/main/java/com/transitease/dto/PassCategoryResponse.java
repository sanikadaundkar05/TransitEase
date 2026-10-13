package com.transitease.dto;

import com.transitease.entity.PassCategory;

public class PassCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Integer validityDays;
    private Double baseFare;
    private String categoryGroup;
    private String eligibility;
    private String mandatoryDocuments;
    private String optionalDocuments;
    private String fareType;
    private String discountDetails;
    private java.util.List<String> mandatoryDocumentList;
    private java.util.List<String> optionalDocumentList;

    public PassCategoryResponse() {
    }

    public PassCategoryResponse(Long id, String name, String description, Integer validityDays, Double baseFare) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.validityDays = validityDays;
        this.baseFare = baseFare;
    }

    public static PassCategoryResponse fromEntity(PassCategory category) {
        if (category == null) {
            return null;
        }
        PassCategoryResponse resp = new PassCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getValidityDays(),
                category.getBaseFare()
        );
        resp.setCategoryGroup(category.getCategoryGroup());
        resp.setEligibility(category.getEligibility());
        resp.setMandatoryDocuments(category.getMandatoryDocuments());
        resp.setOptionalDocuments(category.getOptionalDocuments());
        resp.setFareType(category.getFareType());
        resp.setDiscountDetails(category.getDiscountDetails());
        resp.setMandatoryDocumentList(splitDocuments(category.getMandatoryDocuments()));
        resp.setOptionalDocumentList(splitDocuments(category.getOptionalDocuments()));
        return resp;
    }

    private static java.util.List<String> splitDocuments(String docs) {
        if (docs == null || docs.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return java.util.Arrays.stream(docs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
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

    public java.util.List<String> getMandatoryDocumentList() {
        return mandatoryDocumentList;
    }

    public void setMandatoryDocumentList(java.util.List<String> mandatoryDocumentList) {
        this.mandatoryDocumentList = mandatoryDocumentList;
    }

    public java.util.List<String> getOptionalDocumentList() {
        return optionalDocumentList;
    }

    public void setOptionalDocumentList(java.util.List<String> optionalDocumentList) {
        this.optionalDocumentList = optionalDocumentList;
    }
}
