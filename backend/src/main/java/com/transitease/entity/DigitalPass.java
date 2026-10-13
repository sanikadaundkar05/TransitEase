package com.transitease.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "digital_passes")
public class DigitalPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String passNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_application_id", nullable = false, unique = true)
    private PassApplication application;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate validityStartDate;

    @Column(nullable = false)
    private LocalDate validityEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PassStatus status = PassStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (issueDate == null) {
            issueDate = LocalDate.now();
        }
        if (validityStartDate == null) {
            validityStartDate = LocalDate.now();
        }
        if (status == null) {
            status = PassStatus.ACTIVE;
        }
    }

    public DigitalPass() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassNumber() {
        return passNumber;
    }

    public void setPassNumber(String passNumber) {
        this.passNumber = passNumber;
    }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public PassApplication getApplication() {
        return application;
    }

    public void setApplication(PassApplication application) {
        this.application = application;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getValidityStartDate() {
        return validityStartDate;
    }

    public void setValidityStartDate(LocalDate validityStartDate) {
        this.validityStartDate = validityStartDate;
    }

    public LocalDate getValidityEndDate() {
        return validityEndDate;
    }

    public void setValidityEndDate(LocalDate validityEndDate) {
        this.validityEndDate = validityEndDate;
    }

    public PassStatus getStatus() {
        return status;
    }

    public void setStatus(PassStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
