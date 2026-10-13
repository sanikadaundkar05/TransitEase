package com.transitease.dto;

import com.transitease.entity.PassApplication;
import java.time.LocalDateTime;

public class PassApplicationResponse {
    
    private Long id;
    private Long passCategoryId;
    private String passCategoryName;
    private String passengerName;
    private String passengerEmail;
    private String documentUrl;
    private String status;
    private String remarks;
    private String rejectionReason;
    private Boolean isRenewal;
    private Long renewedPassId;
    private String renewedPassNumber;
    private LocalDateTime applicationDate;

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
    
    public PassApplicationResponse() {}
    
    public static PassApplicationResponse fromEntity(PassApplication application) {
        PassApplicationResponse resp = new PassApplicationResponse();
        resp.setId(application.getId());
        if (application.getPassCategory() != null) {
            resp.setPassCategoryId(application.getPassCategory().getId());
            resp.setPassCategoryName(application.getPassCategory().getName());
        }
        if (application.getPassenger() != null) {
            resp.setPassengerName(application.getPassenger().getFullName());
            resp.setPassengerEmail(application.getPassenger().getEmail());
        }
        resp.setDocumentUrl(application.getDocumentUrl());
        if (application.getStatus() != null) {
            resp.setStatus(application.getStatus().name());
        }
        resp.setRemarks(application.getRemarks());
        resp.setRejectionReason(application.getRemarks());
        resp.setIsRenewal(application.getIsRenewal() != null ? application.getIsRenewal() : false);
        if (application.getRenewedPass() != null) {
            resp.setRenewedPassId(application.getRenewedPass().getId());
            resp.setRenewedPassNumber(application.getRenewedPass().getPassNumber());
        }
        resp.setApplicationDate(application.getApplicationDate());

        // Map personal details
        resp.setFullName(application.getFullName() != null ? application.getFullName() 
                : (application.getPassenger() != null ? application.getPassenger().getFullName() : null));
        resp.setDob(application.getDob());
        resp.setGender(application.getGender());
        resp.setMobileNumber(application.getMobileNumber());
        resp.setEmail(application.getEmail() != null ? application.getEmail() 
                : (application.getPassenger() != null ? application.getPassenger().getEmail() : null));
        resp.setResidentialAddress(application.getResidentialAddress());
        resp.setCity(application.getCity());
        resp.setPincode(application.getPincode());
        resp.setPassengerCategory(application.getPassengerCategory() != null ? application.getPassengerCategory() 
                : (application.getPassCategory() != null ? application.getPassCategory().getCategoryGroup() : null));
        resp.setSourceStop(application.getSourceStop());
        resp.setDestinationStop(application.getDestinationStop());
        resp.setInstitutionName(application.getInstitutionName());
        resp.setEmergencyContact(application.getEmergencyContact());

        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPassCategoryId() {
        return passCategoryId;
    }

    public void setPassCategoryId(Long passCategoryId) {
        this.passCategoryId = passCategoryId;
    }

    public String getPassCategoryName() {
        return passCategoryName;
    }

    public void setPassCategoryName(String passCategoryName) {
        this.passCategoryName = passCategoryName;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    public Long getRenewedPassId() {
        return renewedPassId;
    }

    public void setRenewedPassId(Long renewedPassId) {
        this.renewedPassId = renewedPassId;
    }

    public String getRenewedPassNumber() {
        return renewedPassNumber;
    }

    public void setRenewedPassNumber(String renewedPassNumber) {
        this.renewedPassNumber = renewedPassNumber;
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
