package com.transitease.service;

import com.transitease.dto.DigitalPassResponse;
import com.transitease.entity.DigitalPass;
import com.transitease.entity.PassApplication;
import com.transitease.entity.PassStatus;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.DigitalPassRepository;
import com.transitease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DigitalPassService {

    private final DigitalPassRepository digitalPassRepository;
    private final UserRepository userRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final NotificationService notificationService;

    @Autowired
    public DigitalPassService(DigitalPassRepository digitalPassRepository,
                              UserRepository userRepository,
                              PdfGeneratorService pdfGeneratorService,
                              NotificationService notificationService) {
        this.digitalPassRepository = digitalPassRepository;
        this.userRepository = userRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.notificationService = notificationService;
    }

    public DigitalPassService(DigitalPassRepository digitalPassRepository,
                              UserRepository userRepository,
                              PdfGeneratorService pdfGeneratorService) {
        this(digitalPassRepository, userRepository, pdfGeneratorService, null);
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
    public DigitalPass generatePassForApplication(PassApplication application) {
        if (application == null) {
            throw new IllegalArgumentException("PassApplication cannot be null");
        }

        // Duplicate prevention: return existing pass if already generated for this application
        Optional<DigitalPass> existing = digitalPassRepository.findByApplication(application);
        if (existing.isPresent()) {
            return existing.get();
        }

        DigitalPass pass = new DigitalPass();
        pass.setPassenger(application.getPassenger());
        pass.setApplication(application);
        pass.setPassNumber(generateUniquePassNumber());

        LocalDate today = LocalDate.now();
        pass.setIssueDate(today);
        pass.setValidityStartDate(today);

        int validityDays = 30;
        if (application.getPassCategory() != null && application.getPassCategory().getValidityDays() != null) {
            validityDays = application.getPassCategory().getValidityDays();
        }
        pass.setValidityEndDate(today.plusDays(validityDays));
        pass.setStatus(PassStatus.ACTIVE);

        DigitalPass savedPass = digitalPassRepository.save(pass);
        if (notificationService != null) {
            notificationService.createPassIssuedNotification(savedPass);
        }
        return savedPass;
    }

    @Transactional(readOnly = true)
    public List<DigitalPassResponse> getMyPasses() {
        User currentUser = getAuthenticatedUser();
        return digitalPassRepository.findByPassengerOrderByIssueDateDesc(currentUser).stream()
                .map(DigitalPassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DigitalPassResponse getPassById(Long id) {
        DigitalPass pass = findAndAuthorizePass(id);
        return DigitalPassResponse.fromEntity(pass);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPassPdf(Long id) {
        DigitalPass pass = findAndAuthorizePass(id);
        return pdfGeneratorService.generatePassPdf(pass);
    }

    @Transactional(readOnly = true)
    public List<DigitalPassResponse> getAllPasses() {
        return digitalPassRepository.findAll().stream()
                .map(DigitalPassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private DigitalPass findAndAuthorizePass(Long id) {
        DigitalPass pass = digitalPassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Digital pass not found with id: " + id));

        User currentUser = getAuthenticatedUser();
        // Access control: Passengers can only access their own passes
        if (currentUser.getRole() == Role.PASSENGER) {
            if (pass.getPassenger() == null || !pass.getPassenger().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to access this pass");
            }
        }
        return pass;
    }

    private String generateUniquePassNumber() {
        String passNumber;
        do {
            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            passNumber = "TE-PASS-" + datePart + "-" + randomPart;
        } while (digitalPassRepository.existsByPassNumber(passNumber));
        return passNumber;
    }
}
