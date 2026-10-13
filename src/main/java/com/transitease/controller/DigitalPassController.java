package com.transitease.controller;

import com.transitease.dto.DigitalPassResponse;
import com.transitease.service.DigitalPassService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passes")
public class DigitalPassController {

    private final DigitalPassService digitalPassService;
    private final com.transitease.service.PassApplicationService applicationService;

    public DigitalPassController(DigitalPassService digitalPassService,
                                 com.transitease.service.PassApplicationService applicationService) {
        this.digitalPassService = digitalPassService;
        this.applicationService = applicationService;
    }

    @GetMapping("/my-passes")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<DigitalPassResponse>> getMyPasses() {
        return ResponseEntity.ok(digitalPassService.getMyPasses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<DigitalPassResponse> getPassById(@PathVariable Long id) {
        return ResponseEntity.ok(digitalPassService.getPassById(id));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<byte[]> downloadPassPdf(@PathVariable Long id) {
        DigitalPassResponse pass = digitalPassService.getPassById(id);
        byte[] pdfBytes = digitalPassService.downloadPassPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transit-pass-" + pass.getPassNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<List<DigitalPassResponse>> getAllPasses() {
        return ResponseEntity.ok(digitalPassService.getAllPasses());
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<com.transitease.dto.PassApplicationResponse> renewPass(
            @PathVariable Long id,
            @RequestBody(required = false) com.transitease.dto.PassRenewalRequest request) {
        com.transitease.dto.PassApplicationResponse response = applicationService.renewPass(id, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<com.transitease.dto.PassengerHistoryResponse> getMyHistory() {
        return ResponseEntity.ok(applicationService.getPassengerHistory());
    }
}
