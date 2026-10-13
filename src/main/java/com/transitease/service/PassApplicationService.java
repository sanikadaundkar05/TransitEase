package com.transitease.service;

import com.transitease.dto.PassApplicationRequest;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.PassApplication;
import com.transitease.entity.PassCategory;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.PassApplicationRepository;
import com.transitease.repository.PassCategoryRepository;
import com.transitease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassApplicationService {

    private final PassApplicationRepository applicationRepository;
    private final PassCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final DigitalPassService digitalPassService;
    private final com.transitease.repository.DigitalPassRepository digitalPassRepository;
    private final NotificationService notificationService;

    @Autowired
    public PassApplicationService(PassApplicationRepository applicationRepository, 
                                  PassCategoryRepository categoryRepository,
                                  UserRepository userRepository,
                                  DigitalPassService digitalPassService,
                                  com.transitease.repository.DigitalPassRepository digitalPassRepository,
                                  NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.digitalPassService = digitalPassService;
        this.digitalPassRepository = digitalPassRepository;
        this.notificationService = notificationService;
    }

    public PassApplicationService(PassApplicationRepository applicationRepository, 
                                  PassCategoryRepository categoryRepository,
                                  UserRepository userRepository,
                                  DigitalPassService digitalPassService,
                                  com.transitease.repository.DigitalPassRepository digitalPassRepository) {
        this(applicationRepository, categoryRepository, userRepository, digitalPassService, digitalPassRepository, null);
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        }
        throw new RuntimeException("User not authenticated");
    }

    @Transactional
    public PassApplicationResponse applyForPass(PassApplicationRequest request) {
        User passenger = getAuthenticatedUser();
        PassCategory category = categoryRepository.findById(request.getPassCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Pass Category not found with id: " + request.getPassCategoryId()));

        PassApplication application = new PassApplication();
        application.setPassenger(passenger);
        application.setPassCategory(category);
        application.setDocumentUrl(request.getDocumentUrl());
        application.setStatus(ApplicationStatus.PENDING);

        // Populate passenger personal details
        application.setFullName(request.getFullName() != null && !request.getFullName().trim().isEmpty()
                ? request.getFullName().trim() : passenger.getFullName());
        application.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty()
                ? request.getEmail().trim() : passenger.getEmail());
        application.setDob(request.getDob());
        application.setGender(request.getGender());
        application.setMobileNumber(request.getMobileNumber());
        application.setResidentialAddress(request.getResidentialAddress());
        application.setCity(request.getCity());
        application.setPincode(request.getPincode());
        application.setPassengerCategory(request.getPassengerCategory() != null && !request.getPassengerCategory().trim().isEmpty()
                ? request.getPassengerCategory().trim() : category.getCategoryGroup());
        application.setSourceStop(request.getSourceStop());
        application.setDestinationStop(request.getDestinationStop());
        application.setInstitutionName(request.getInstitutionName());
        application.setEmergencyContact(request.getEmergencyContact());

        PassApplication saved = applicationRepository.save(application);
        return PassApplicationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PassApplicationResponse> getMyApplications() {
        User passenger = getAuthenticatedUser();
        return applicationRepository.findByPassengerOrderByApplicationDateDesc(passenger).stream()
                .map(PassApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PassApplicationResponse renewPass(Long passId, com.transitease.dto.PassRenewalRequest request) {
        com.transitease.entity.DigitalPass pass = digitalPassRepository.findById(passId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital pass not found with id: " + passId));

        User currentUser = getAuthenticatedUser();

        // Access control: Passengers cannot renew another passenger's pass
        if (currentUser.getRole() == Role.PASSENGER) {
            if (pass.getPassenger() == null || !pass.getPassenger().getId().equals(currentUser.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("You cannot renew another passenger's pass");
            }
        }

        // Prevent duplicate renewal applications for the same pass when one is already pending or under review
        boolean hasPending = applicationRepository.existsByRenewedPassIdAndStatusIn(
                passId, List.of(ApplicationStatus.PENDING, ApplicationStatus.VERIFIED)
        );
        if (hasPending) {
            throw new IllegalStateException("A renewal application is already pending or under review for this pass");
        }

        // Determine pass category
        Long targetCategoryId = (request != null && request.getPassCategoryId() != null)
                ? request.getPassCategoryId()
                : (pass.getApplication() != null && pass.getApplication().getPassCategory() != null
                    ? pass.getApplication().getPassCategory().getId()
                    : null);

        if (targetCategoryId == null) {
            throw new IllegalArgumentException("Pass category must be specified for renewal");
        }

        PassCategory category = categoryRepository.findById(targetCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Pass Category not found with id: " + targetCategoryId));

        String docUrl = (request != null && request.getDocumentUrl() != null && !request.getDocumentUrl().trim().isEmpty())
                ? request.getDocumentUrl().trim()
                : (pass.getApplication() != null ? pass.getApplication().getDocumentUrl() : null);

        PassApplication renewalApp = new PassApplication();
        renewalApp.setPassenger(pass.getPassenger());
        renewalApp.setPassCategory(category);
        renewalApp.setDocumentUrl(docUrl);
        renewalApp.setStatus(ApplicationStatus.PENDING);
        renewalApp.setIsRenewal(true);
        renewalApp.setRenewedPass(pass);

        if (pass.getApplication() != null) {
            PassApplication prev = pass.getApplication();
            renewalApp.setFullName(prev.getFullName() != null ? prev.getFullName() : pass.getPassenger().getFullName());
            renewalApp.setEmail(prev.getEmail() != null ? prev.getEmail() : pass.getPassenger().getEmail());
            renewalApp.setDob(prev.getDob());
            renewalApp.setGender(prev.getGender());
            renewalApp.setMobileNumber(prev.getMobileNumber());
            renewalApp.setResidentialAddress(prev.getResidentialAddress());
            renewalApp.setCity(prev.getCity());
            renewalApp.setPincode(prev.getPincode());
            renewalApp.setPassengerCategory(prev.getPassengerCategory());
            renewalApp.setSourceStop(prev.getSourceStop());
            renewalApp.setDestinationStop(prev.getDestinationStop());
            renewalApp.setInstitutionName(prev.getInstitutionName());
            renewalApp.setEmergencyContact(prev.getEmergencyContact());
        }

        PassApplication saved = applicationRepository.save(renewalApp);
        return PassApplicationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public com.transitease.dto.PassengerHistoryResponse getPassengerHistory() {
        User passenger = getAuthenticatedUser();
        List<PassApplicationResponse> applications = applicationRepository
                .findByPassengerOrderByApplicationDateDesc(passenger).stream()
                .map(PassApplicationResponse::fromEntity)
                .collect(Collectors.toList());

        List<com.transitease.dto.DigitalPassResponse> passes = digitalPassRepository
                .findByPassengerOrderByIssueDateDesc(passenger).stream()
                .map(com.transitease.dto.DigitalPassResponse::fromEntity)
                .collect(Collectors.toList());

        return new com.transitease.dto.PassengerHistoryResponse(applications, passes);
    }

    @Transactional
    public PassApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus) {
        return updateApplicationStatus(id, newStatus, null);
    }

    @Transactional
    public PassApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus, String remarks) {
        PassApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        validateStatusTransition(application.getStatus(), newStatus);

        application.setStatus(newStatus);
        if (remarks != null && !remarks.trim().isEmpty()) {
            application.setRemarks(remarks.trim());
        }

        PassApplication updated = applicationRepository.save(application);

        // Notify passenger about application status transition
        if (notificationService != null) {
            notificationService.createApplicationStatusNotification(updated, newStatus, remarks);
        }

        if (newStatus == ApplicationStatus.APPROVED) {
            digitalPassService.generatePassForApplication(updated);
        }

        return PassApplicationResponse.fromEntity(updated);
    }

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Target status cannot be null");
        }

        if (currentStatus == newStatus) {
            throw new IllegalStateException("Application is already in status: " + currentStatus);
        }

        switch (currentStatus) {
            case PENDING -> {
                if (newStatus == ApplicationStatus.APPROVED) {
                    throw new IllegalStateException("Cannot approve an application before document verification. Current status is PENDING.");
                }
                if (newStatus != ApplicationStatus.VERIFIED && newStatus != ApplicationStatus.REJECTED) {
                    throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
            }
            case VERIFIED -> {
                if (newStatus != ApplicationStatus.APPROVED && newStatus != ApplicationStatus.REJECTED) {
                    throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
            }
            case APPROVED -> {
                throw new IllegalStateException("Application is already APPROVED and cannot be modified.");
            }
            case REJECTED -> {
                throw new IllegalStateException("Application is already REJECTED and cannot be modified.");
            }
            default -> throw new IllegalStateException("Unsupported application status: " + currentStatus);
        }
    }
    
    @Transactional(readOnly = true)
    public List<PassApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(PassApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
