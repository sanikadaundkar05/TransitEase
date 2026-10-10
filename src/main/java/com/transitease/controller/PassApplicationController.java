package com.transitease.controller;

import com.transitease.dto.PassApplicationRequest;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.ApplicationStatus;
import com.transitease.service.PassApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pass-applications")
public class PassApplicationController {

    private final PassApplicationService applicationService;

    public PassApplicationController(PassApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PassApplicationResponse> applyForPass(@Valid @RequestBody PassApplicationRequest request) {
        PassApplicationResponse response = applicationService.applyForPass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<PassApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(applicationService.getMyApplications());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<List<PassApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<PassApplicationResponse> updateStatus(@PathVariable Long id, @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, status));
    }
}
