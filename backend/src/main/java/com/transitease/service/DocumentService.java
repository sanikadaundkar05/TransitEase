package com.transitease.service;

import com.transitease.dto.DocumentResponse;
import com.transitease.entity.ApplicationDocument;
import com.transitease.entity.PassApplication;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.ApplicationDocumentRepository;
import com.transitease.repository.PassApplicationRepository;
import com.transitease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf", ".jpg", ".jpeg", ".png");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/jpg", "image/png");

    private final ApplicationDocumentRepository documentRepository;
    private final PassApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final String uploadDir;

    public DocumentService(ApplicationDocumentRepository documentRepository,
                           PassApplicationRepository applicationRepository,
                           UserRepository userRepository,
                           @Value("${file.upload-dir:uploads/documents}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.uploadDir = uploadDir;
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
    public DocumentResponse uploadDocument(Long applicationId, MultipartFile file) {
        return uploadDocument(applicationId, file, null);
    }

    @Transactional
    public DocumentResponse uploadDocument(Long applicationId, MultipartFile file, String documentType) {
        validateFile(file);

        PassApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Pass application not found with id: " + applicationId));

        User currentUser = getAuthenticatedUser();

        // Ownership rule: Passengers can only upload documents for their own applications
        if (currentUser.getRole() == Role.PASSENGER) {
            if (application.getPassenger() == null || !application.getPassenger().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only upload documents for your own application");
            }
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid file name: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename);
        String storedFileName = "app_" + applicationId + "_" + UUID.randomUUID().toString() + extension;

        Path targetDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDirectory);
            Path targetFile = targetDirectory.resolve(storedFileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            ApplicationDocument doc = new ApplicationDocument();
            doc.setApplication(application);
            doc.setUploadedBy(currentUser);
            doc.setFileName(originalFilename);
            doc.setStoredFileName(storedFileName);
            doc.setContentType(file.getContentType());
            doc.setFileSize(file.getSize());
            doc.setFilePath(targetFile.toString());
            doc.setDocumentType(documentType != null && !documentType.trim().isEmpty() ? documentType.trim() : "Supporting Document");
            doc.setStatus("PENDING");
            doc.setUploadDate(LocalDateTime.now());

            ApplicationDocument saved = documentRepository.save(doc);

            // Update application documentUrl to point to this uploaded document
            application.setDocumentUrl("/api/documents/" + saved.getId() + "/download");

            // If the application was REJECTED, reset it to PENDING when replacement document is uploaded
            if (application.getStatus() == com.transitease.entity.ApplicationStatus.REJECTED) {
                application.setStatus(com.transitease.entity.ApplicationStatus.PENDING);
                application.setRemarks("Document updated by applicant (" + doc.getDocumentType() + ") - Pending re-verification");
            }

            applicationRepository.save(application);

            return DocumentResponse.fromEntity(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    @Transactional
    public DocumentResponse updateDocumentStatus(Long documentId, String status, String remarks) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!normalizedStatus.equals("VERIFIED") && !normalizedStatus.equals("REJECTED") && !normalizedStatus.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid document status: " + status);
        }

        ApplicationDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        doc.setStatus(normalizedStatus);
        if (remarks != null && !remarks.trim().isEmpty()) {
            doc.setRemarks(remarks.trim());
        }

        ApplicationDocument saved = documentRepository.save(doc);

        // If rejected, update application remarks to reflect document rejection
        if (normalizedStatus.equals("REJECTED")) {
            PassApplication app = doc.getApplication();
            if (app != null) {
                String docName = doc.getDocumentType() != null ? doc.getDocumentType() : doc.getFileName();
                String reason = remarks != null && !remarks.trim().isEmpty() ? remarks.trim() : "Document rejected by officer";
                app.setRemarks("Document rejected (" + docName + "): " + reason);
                applicationRepository.save(app);
            }
        }

        return DocumentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsForApplication(Long applicationId) {
        PassApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Pass application not found with id: " + applicationId));

        User currentUser = getAuthenticatedUser();
        // Access control: Passengers can only view documents for their own applications
        if (currentUser.getRole() == Role.PASSENGER) {
            if (application.getPassenger() == null || !application.getPassenger().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to view documents for this application");
            }
        }

        return documentRepository.findByApplication(application).stream()
                .map(DocumentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentMetadata(Long documentId) {
        ApplicationDocument doc = findAndAuthorizeDocument(documentId);
        return DocumentResponse.fromEntity(doc);
    }

    @Transactional(readOnly = true)
    public byte[] loadDocumentContent(Long documentId) {
        ApplicationDocument doc = findAndAuthorizeDocument(documentId);

        Path filePath = Paths.get(doc.getFilePath()).toAbsolutePath().normalize();
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ResourceNotFoundException("Document file not found on disk");
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading document file: " + doc.getFileName(), e);
        }
    }

    @Transactional(readOnly = true)
    public ApplicationDocument getDocumentEntity(Long documentId) {
        return findAndAuthorizeDocument(documentId);
    }

    private ApplicationDocument findAndAuthorizeDocument(Long documentId) {
        ApplicationDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User currentUser = getAuthenticatedUser();
        // Access control: Passengers can only access documents from their own applications
        if (currentUser.getRole() == Role.PASSENGER) {
            PassApplication app = doc.getApplication();
            if (app == null || app.getPassenger() == null || !app.getPassenger().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to access this document");
            }
        }
        return doc;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5 MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String extension = getFileExtension(originalFilename).toLowerCase(Locale.ROOT);
        String contentType = file.getContentType();

        boolean validExtension = ALLOWED_EXTENSIONS.contains(extension);
        boolean validContentType = contentType != null && ALLOWED_CONTENT_TYPES.stream()
                .anyMatch(ct -> ct.equalsIgnoreCase(contentType.trim()));

        if (!validExtension || !validContentType) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG files are supported");
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0) {
            return filename.substring(dotIndex);
        }
        return "";
    }
}
