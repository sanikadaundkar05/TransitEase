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

    public PassCategory() {
    }

    public PassCategory(Long id, String name, String description, Integer validityDays, Double baseFare) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.validityDays = validityDays;
        this.baseFare = baseFare;
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
