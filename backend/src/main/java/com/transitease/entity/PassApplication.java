package com.transitease.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pass_applications")
public class PassApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_category_id", nullable = false)
    private PassCategory passCategory;

    @Column(length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(length = 1000)
    private String remarks;

    @Column(nullable = false)
    private Boolean isRenewal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewed_pass_id")
    private DigitalPass renewedPass;

    @Column(length = 255)
    private String fullName;

    @Column(length = 50)
    private String dob;

    @Column(length = 20)
    private String gender;

    @Column(length = 20)
    private String mobileNumber;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String residentialAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String pincode;

    @Column(length = 50)
    private String passengerCategory;

    @Column(length = 255)
    private String sourceStop;

    @Column(length = 255)
    private String destinationStop;

    @Column(length = 255)
    private String institutionName;

    @Column(length = 100)
    private String emergencyContact;

    @Column(nullable = false, updatable = false)
    private LocalDateTime applicationDate;

    @PrePersist
    protected void onCreate() {
        if (applicationDate == null) {
            applicationDate = LocalDateTime.now();
        }
        if (isRenewal == null) {
            isRenewal = false;
        }
    }

    public PassApplication() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public PassCategory getPassCategory() {
        return passCategory;
    }

    public void setPassCategory(PassCategory passCategory) {
        this.passCategory = passCategory;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRejectionReason() {
        return remarks;
    }

    public void setRejectionReason(String rejectionReason) {
        this.remarks = rejectionReason;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Boolean getIsRenewal() {
        return isRenewal;
    }

    public void setIsRenewal(Boolean isRenewal) {
        this.isRenewal = isRenewal;
    }

    public DigitalPass getRenewedPass() {
        return renewedPass;
    }

    public void setRenewedPass(DigitalPass renewedPass) {
        this.renewedPass = renewedPass;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getPassengerCategory() {
        return passengerCategory;
    }

    public void setPassengerCategory(String passengerCategory) {
        this.passengerCategory = passengerCategory;
    }

    public String getSourceStop() {
        return sourceStop;
    }

    public void setSourceStop(String sourceStop) {
        this.sourceStop = sourceStop;
    }

    public String getDestinationStop() {
        return destinationStop;
    }

    public void setDestinationStop(String destinationStop) {
        this.destinationStop = destinationStop;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
