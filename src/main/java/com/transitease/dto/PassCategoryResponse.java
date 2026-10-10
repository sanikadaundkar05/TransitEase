package com.transitease.dto;

import com.transitease.entity.PassCategory;

public class PassCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Integer validityDays;
    private Double baseFare;

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
        return new PassCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getValidityDays(),
                category.getBaseFare()
        );
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
}
