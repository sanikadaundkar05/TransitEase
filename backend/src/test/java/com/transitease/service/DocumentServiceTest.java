package com.transitease.service;

import com.transitease.dto.DocumentResponse;
import com.transitease.entity.ApplicationDocument;
import com.transitease.entity.PassApplication;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.ApplicationDocumentRepository;
import com.transitease.repository.PassApplicationRepository;
import com.transitease.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private PassApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @TempDir
    Path tempUploadDir;

    private DocumentService documentService;

    private User passenger1;
    private User passenger2;
    private User officerUser;
    private PassApplication app1;
    private ApplicationDocument sampleDoc;

    @BeforeEach
    void setUp() throws IOException {
        documentService = new DocumentService(documentRepository, applicationRepository, userRepository, tempUploadDir.toString());

        passenger1 = new User();
        passenger1.setId(10L);
        passenger1.setFullName("Passenger One");
        passenger1.setEmail("passenger1@transitease.com");
        passenger1.setRole(Role.PASSENGER);

        passenger2 = new User();
        passenger2.setId(20L);
        passenger2.setFullName("Passenger Two");
        passenger2.setEmail("passenger2@transitease.com");
        passenger2.setRole(Role.PASSENGER);

        officerUser = new User();
        officerUser.setId(30L);
        officerUser.setFullName("Verification Officer");
        officerUser.setEmail("officer@transitease.com");
        officerUser.setRole(Role.DOCUMENT_VERIFICATION_OFFICER);

        app1 = new PassApplication();
        app1.setId(100L);
        app1.setPassenger(passenger1);

        Path testFile = tempUploadDir.resolve("sample.pdf");
        Files.write(testFile, "PDF Content".getBytes());

        sampleDoc = new ApplicationDocument();
        sampleDoc.setId(50L);
        sampleDoc.setApplication(app1);
        sampleDoc.setUploadedBy(passenger1);
        sampleDoc.setFileName("sample.pdf");
        sampleDoc.setStoredFileName("sample.pdf");
        sampleDoc.setContentType("application/pdf");
        sampleDoc.setFileSize(11L);
        sampleDoc.setFilePath(testFile.toString());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email, String role) {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        email, "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testUploadDocument_ValidPdf_Success() {
        authenticateAs("passenger1@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger1@transitease.com")).thenReturn(Optional.of(passenger1));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(invocation -> {
            ApplicationDocument doc = invocation.getArgument(0);
            doc.setId(1L);
            return doc;
        });

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "student_id.pdf",
                "application/pdf",
                "PDF Content bytes".getBytes()
        );

        DocumentResponse response = documentService.uploadDocument(100L, file);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("student_id.pdf", response.getFileName());
        assertEquals("application/pdf", response.getContentType());
        assertEquals("/api/documents/1/download", response.getDownloadUrl());

        verify(documentRepository, times(1)).save(any(ApplicationDocument.class));
        verify(applicationRepository, times(1)).save(app1);
        assertEquals("/api/documents/1/download", app1.getDocumentUrl());
    }

    @Test
    void testUploadDocument_ValidImageJpgAndPng_Success() {
        authenticateAs("passenger1@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger1@transitease.com")).thenReturn(Optional.of(passenger1));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile jpgFile = new MockMultipartFile("file", "id_card.jpg", "image/jpeg", "fake jpg".getBytes());
        DocumentResponse jpgResponse = documentService.uploadDocument(100L, jpgFile);
        assertNotNull(jpgResponse);
        assertEquals("id_card.jpg", jpgResponse.getFileName());

        MockMultipartFile pngFile = new MockMultipartFile("file", "photo.png", "image/png", "fake png".getBytes());
        DocumentResponse pngResponse = documentService.uploadDocument(100L, pngFile);
        assertNotNull(pngResponse);
        assertEquals("photo.png", pngResponse.getFileName());
    }

    @Test
    void testUploadDocument_InvalidFileType_ThrowsIllegalArgumentException() {
        MockMultipartFile txtFile = new MockMultipartFile("file", "notes.txt", "text/plain", "plain text".getBytes());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                documentService.uploadDocument(100L, txtFile)
        );

        assertTrue(exception.getMessage().contains("Only PDF, JPG, and PNG files are supported"));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void testUploadDocument_OversizedFile_ThrowsIllegalArgumentException() {
        byte[] oversizedBytes = new byte[(int) (5.5 * 1024 * 1024)]; // 5.5 MB
        MockMultipartFile bigFile = new MockMultipartFile("file", "large.pdf", "application/pdf", oversizedBytes);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                documentService.uploadDocument(100L, bigFile)
        );

        assertTrue(exception.getMessage().contains("File size exceeds maximum limit of 5 MB"));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void testUploadDocument_EmptyFile_ThrowsIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                documentService.uploadDocument(100L, emptyFile)
        );

        assertTrue(exception.getMessage().contains("File cannot be empty"));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void testUploadDocument_DifferentPassenger_ThrowsAccessDeniedException() {
        authenticateAs("passenger2@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger2@transitease.com")).thenReturn(Optional.of(passenger2));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1)); // app1 belongs to passenger1

        MockMultipartFile file = new MockMultipartFile("file", "id.pdf", "application/pdf", "data".getBytes());

        assertThrows(AccessDeniedException.class, () -> documentService.uploadDocument(100L, file));
        verify(documentRepository, never()).save(any());
    }

    @Test
    void testGetDocumentsForApplication_OwnerPassenger_Success() {
        authenticateAs("passenger1@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger1@transitease.com")).thenReturn(Optional.of(passenger1));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1));
        when(documentRepository.findByApplication(app1)).thenReturn(List.of(sampleDoc));

        List<DocumentResponse> docs = documentService.getDocumentsForApplication(100L);

        assertNotNull(docs);
        assertEquals(1, docs.size());
        assertEquals("sample.pdf", docs.get(0).getFileName());
    }

    @Test
    void testGetDocumentsForApplication_DifferentPassenger_ThrowsAccessDeniedException() {
        authenticateAs("passenger2@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger2@transitease.com")).thenReturn(Optional.of(passenger2));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1)); // app1 belongs to passenger1

        assertThrows(AccessDeniedException.class, () -> documentService.getDocumentsForApplication(100L));
    }

    @Test
    void testGetDocumentsForApplication_Officer_Success() {
        authenticateAs("officer@transitease.com", "DOCUMENT_VERIFICATION_OFFICER");
        when(userRepository.findByEmail("officer@transitease.com")).thenReturn(Optional.of(officerUser));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1));
        when(documentRepository.findByApplication(app1)).thenReturn(List.of(sampleDoc));

        List<DocumentResponse> docs = documentService.getDocumentsForApplication(100L);

        assertNotNull(docs);
        assertEquals(1, docs.size());
    }

    @Test
    void testLoadDocumentContent_OwnerPassenger_ReturnsBytes() {
        authenticateAs("passenger1@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger1@transitease.com")).thenReturn(Optional.of(passenger1));
        when(documentRepository.findById(50L)).thenReturn(Optional.of(sampleDoc));

        byte[] content = documentService.loadDocumentContent(50L);

        assertNotNull(content);
        assertEquals("PDF Content", new String(content));
    }

    @Test
    void testLoadDocumentContent_DifferentPassenger_ThrowsAccessDeniedException() {
        authenticateAs("passenger2@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger2@transitease.com")).thenReturn(Optional.of(passenger2));
        when(documentRepository.findById(50L)).thenReturn(Optional.of(sampleDoc)); // belongs to passenger1

        assertThrows(AccessDeniedException.class, () -> documentService.loadDocumentContent(50L));
    }

    @Test
    void testLoadDocumentContent_Officer_ReturnsBytes() {
        authenticateAs("officer@transitease.com", "DOCUMENT_VERIFICATION_OFFICER");
        when(userRepository.findByEmail("officer@transitease.com")).thenReturn(Optional.of(officerUser));
        when(documentRepository.findById(50L)).thenReturn(Optional.of(sampleDoc));

        byte[] content = documentService.loadDocumentContent(50L);

        assertNotNull(content);
        assertEquals("PDF Content", new String(content));
    }

    @Test
    void testUploadDocument_WithDocumentType_StoresType() {
        authenticateAs("passenger1@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger1@transitease.com")).thenReturn(Optional.of(passenger1));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "bonafide.pdf", "application/pdf", "Bonafide Content".getBytes());

        DocumentResponse response = documentService.uploadDocument(100L, file, "Bonafide Certificate");

        assertNotNull(response);
        assertEquals("Bonafide Certificate", response.getDocumentType());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void testUploadDocument_WhenApplicationWasRejected_ResetsStatusToPending() {
        app1.setStatus(com.transitease.entity.ApplicationStatus.REJECTED);
        app1.setRemarks("Document rejected: Unclear student ID");

        authenticateAs("passenger1@transitease.com", "PASSENGER");
        when(userRepository.findByEmail("passenger1@transitease.com")).thenReturn(Optional.of(passenger1));
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app1));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "clear_id.pdf", "application/pdf", "Clear ID bytes".getBytes());

        DocumentResponse response = documentService.uploadDocument(100L, file, "Student ID Card");

        assertNotNull(response);
        assertEquals(com.transitease.entity.ApplicationStatus.PENDING, app1.getStatus());
        assertTrue(app1.getRemarks().contains("Pending re-verification"));
    }

    @Test
    void testUpdateDocumentStatus_Verify_Success() {
        when(documentRepository.findById(50L)).thenReturn(Optional.of(sampleDoc));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response = documentService.updateDocumentStatus(50L, "VERIFIED", "Legible and valid");

        assertNotNull(response);
        assertEquals("VERIFIED", response.getStatus());
        assertEquals("Legible and valid", response.getRemarks());
    }

    @Test
    void testUpdateDocumentStatus_Reject_UpdatesApplicationRemarks() {
        when(documentRepository.findById(50L)).thenReturn(Optional.of(sampleDoc));
        when(documentRepository.save(any(ApplicationDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response = documentService.updateDocumentStatus(50L, "REJECTED", "Blurry document text");

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("Blurry document text", response.getRemarks());
        assertTrue(app1.getRemarks().contains("Blurry document text"));
        verify(applicationRepository, times(1)).save(app1);
    }
}
