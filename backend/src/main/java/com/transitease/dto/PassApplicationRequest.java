package com.transitease.dto;

import jakarta.validation.constraints.NotNull;

public class PassApplicationRequest {
    
    @NotNull(message = "Pass category ID is required")
    private Long passCategoryId;

    private String documentUrl;

    // Passenger Personal Details
    private String fullName;
    private String dob;
    private String gender;
    private String mobileNumber;
    private String email;
    private String residentialAddress;
    private String city;
    private String pincode;
    private String passengerCategory;
    private String sourceStop;
    private String destinationStop;
    private String institutionName;
    private String emergencyContact;

    public PassApplicationRequest() {
    }

    public PassApplicationRequest(Long passCategoryId, String documentUrl) {
        this.passCategoryId = passCategoryId;
        this.documentUrl = documentUrl;
    }

    public Long getPassCategoryId() {
        return passCategoryId;
    }

    public void setPassCategoryId(Long passCategoryId) {
        this.passCategoryId = passCategoryId;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
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
