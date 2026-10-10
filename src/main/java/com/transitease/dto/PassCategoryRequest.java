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
    private Double baseFare;

    public PassCategoryRequest() {
    }

    public PassCategoryRequest(String name, String description, Integer validityDays, Double baseFare) {
        this.name = name;
        this.description = description;
        this.validityDays = validityDays;
        this.baseFare = baseFare;
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
