package com.transitease.service;

import com.transitease.dto.PassApplicationRequest;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.PassApplication;
import com.transitease.entity.PassCategory;
import com.transitease.entity.User;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.PassApplicationRepository;
import com.transitease.repository.PassCategoryRepository;
import com.transitease.repository.UserRepository;
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

    public PassApplicationService(PassApplicationRepository applicationRepository, 
                                  PassCategoryRepository categoryRepository,
                                  UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
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

        PassApplication saved = applicationRepository.save(application);
        return PassApplicationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PassApplicationResponse> getMyApplications() {
        User passenger = getAuthenticatedUser();
        return applicationRepository.findByPassenger(passenger).stream()
                .map(PassApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PassApplicationResponse updateApplicationStatus(Long id, ApplicationStatus newStatus) {
        PassApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        
        application.setStatus(newStatus);
        PassApplication updated = applicationRepository.save(application);
        return PassApplicationResponse.fromEntity(updated);
    }
    
    @Transactional(readOnly = true)
    public List<PassApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(PassApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
