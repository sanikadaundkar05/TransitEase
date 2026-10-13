package com.transitease.controller;

import com.transitease.dto.DocumentResponse;
import com.transitease.entity.ApplicationDocument;
import com.transitease.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType) {
        DocumentResponse response = documentService.uploadDocument(applicationId, file, documentType);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCUMENT_VERIFICATION_OFFICER', 'ADMIN')")
    public ResponseEntity<DocumentResponse> updateDocumentStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        String remarks = body != null ? body.get("remarks") : null;
        DocumentResponse response = documentService.updateDocumentStatus(id, status, remarks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<DocumentResponse> getDocumentMetadata(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentMetadata(id));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN', 'DOCUMENT_VERIFICATION_OFFICER', 'PASS_APPROVAL_OFFICER')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        ApplicationDocument doc = documentService.getDocumentEntity(id);
        byte[] content = documentService.loadDocumentContent(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (doc.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(doc.getContentType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(content);
    }
}
