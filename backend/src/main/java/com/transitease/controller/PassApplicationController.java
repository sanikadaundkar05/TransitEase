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
    private final com.transitease.service.DocumentService documentService;

    public PassApplicationController(PassApplicationService applicationService,
                                   com.transitease.service.DocumentService documentService) {
        this.applicationService = applicationService;
        this.documentService = documentService;
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
    public ResponseEntity<PassApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String remarks,
            @RequestBody(required = false) com.transitease.dto.UpdateStatusRequest requestBody) {

        ApplicationStatus targetStatus = (requestBody != null && requestBody.getStatus() != null)
                ? requestBody.getStatus()
                : status;

        String targetRemarks = (requestBody != null && requestBody.getRemarks() != null)
                ? requestBody.getRemarks()
                : remarks;

        if (targetStatus == null) {
            throw new IllegalArgumentException("Status parameter is required");
        }

        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, targetStatus, targetRemarks));
    }

    @PostMapping(value = "/{id}/documents", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<com.transitease.dto.DocumentResponse> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType) {
        com.transitease.dto.DocumentResponse response = documentService.uploadDocument(id, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<List<com.transitease.dto.DocumentResponse>> getDocumentsForApplication(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentsForApplication(id));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<com.transitease.dto.PassengerHistoryResponse> getMyHistory() {
        return ResponseEntity.ok(applicationService.getPassengerHistory());
    }

    @PostMapping("/renew/{passId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PassApplicationResponse> renewPass(
            @PathVariable Long passId,
            @RequestBody(required = false) com.transitease.dto.PassRenewalRequest request) {
        PassApplicationResponse response = applicationService.renewPass(passId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
