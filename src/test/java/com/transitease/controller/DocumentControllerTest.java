package com.transitease.controller;

import com.transitease.dto.DocumentResponse;
import com.transitease.entity.ApplicationDocument;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private UserRepository userRepository;

    private String passengerToken;
    private String officerToken;
    private DocumentResponse sampleResponse;
    private ApplicationDocument sampleEntity;

    @BeforeEach
    void setUp() {
        User passenger = new User();
        passenger.setId(10L);
        passenger.setFullName("Passenger User");
        passenger.setEmail("passenger@transitease.com");
        passenger.setRole(Role.PASSENGER);

        User officer = new User();
        officer.setId(20L);
        officer.setFullName("Verification Officer");
        officer.setEmail("officer@transitease.com");
        officer.setRole(Role.DOCUMENT_VERIFICATION_OFFICER);

        when(userRepository.findByEmail("passenger@transitease.com")).thenReturn(Optional.of(passenger));
        when(userRepository.findByEmail("officer@transitease.com")).thenReturn(Optional.of(officer));

        passengerToken = jwtService.generateToken(passenger);
        officerToken = jwtService.generateToken(officer);

        sampleResponse = new DocumentResponse();
        sampleResponse.setId(1L);
        sampleResponse.setApplicationId(100L);
        sampleResponse.setFileName("id_card.pdf");
        sampleResponse.setContentType("application/pdf");
        sampleResponse.setFileSize(2048L);
        sampleResponse.setDownloadUrl("/api/documents/1/download");

        sampleEntity = new ApplicationDocument();
        sampleEntity.setId(1L);
        sampleEntity.setFileName("id_card.pdf");
        sampleEntity.setContentType("application/pdf");
    }

    @Test
    void testUploadDocument_ViaPassApplicationEndpoint_Success() throws Exception {
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class), any())).thenReturn(sampleResponse);
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class))).thenReturn(sampleResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file", "id_card.pdf", "application/pdf", "Dummy PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/pass-applications/100/documents")
                        .file(file)
                        .param("documentType", "Identity Proof")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("id_card.pdf"))
                .andExpect(jsonPath("$.downloadUrl").value("/api/documents/1/download"));
    }

    @Test
    void testUploadDocument_ViaDocumentController_Success() throws Exception {
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class), any())).thenReturn(sampleResponse);
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class))).thenReturn(sampleResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file", "id_card.pdf", "application/pdf", "Dummy PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("applicationId", "100")
                        .param("documentType", "Aadhaar Card")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("id_card.pdf"));
    }

    @Test
    void testUploadDocument_InvalidFileType_Returns400() throws Exception {
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class), any()))
                .thenThrow(new IllegalArgumentException("Only PDF, JPG, and PNG files are supported"));
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Only PDF, JPG, and PNG files are supported"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "bad".getBytes()
        );

        mockMvc.perform(multipart("/api/pass-applications/100/documents")
                        .file(file)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Only PDF, JPG, and PNG files are supported"));
    }

    @Test
    void testUploadDocument_OversizedFile_Returns400() throws Exception {
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class), any()))
                .thenThrow(new IllegalArgumentException("File size exceeds maximum limit of 5 MB"));
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class)))
                .thenThrow(new IllegalArgumentException("File size exceeds maximum limit of 5 MB"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", "too large".getBytes()
        );

        mockMvc.perform(multipart("/api/pass-applications/100/documents")
                        .file(file)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("File size exceeds maximum limit of 5 MB"));
    }

    @Test
    void testUploadDocument_UnauthorizedPassenger_Returns403() throws Exception {
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class), any()))
                .thenThrow(new AccessDeniedException("You can only upload documents for your own application"));
        when(documentService.uploadDocument(eq(100L), any(MockMultipartFile.class)))
                .thenThrow(new AccessDeniedException("You can only upload documents for your own application"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "id.pdf", "application/pdf", "data".getBytes()
        );

        mockMvc.perform(multipart("/api/pass-applications/100/documents")
                        .file(file)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You can only upload documents for your own application"));
    }

    @Test
    void testUpdateDocumentStatus_AsOfficer_Success() throws Exception {
        DocumentResponse verifiedResp = new DocumentResponse();
        verifiedResp.setId(1L);
        verifiedResp.setDocumentType("Aadhaar Card");
        verifiedResp.setStatus("VERIFIED");
        verifiedResp.setRemarks("Valid identity proof");

        when(documentService.updateDocumentStatus(eq(1L), eq("VERIFIED"), eq("Valid identity proof")))
                .thenReturn(verifiedResp);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/documents/1/status")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"VERIFIED\", \"remarks\": \"Valid identity proof\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.remarks").value("Valid identity proof"));
    }

    @Test
    void testUpdateDocumentStatus_AsPassenger_ReturnsForbidden() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/documents/1/status")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"VERIFIED\", \"remarks\": \"I am passenger\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDownloadDocument_OwnerPassenger_Success() throws Exception {
        byte[] content = "PDF bytes data".getBytes();
        when(documentService.getDocumentEntity(1L)).thenReturn(sampleEntity);
        when(documentService.loadDocumentContent(1L)).thenReturn(content);

        mockMvc.perform(get("/api/documents/1/download")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"id_card.pdf\""))
                .andExpect(content().bytes(content));
    }

    @Test
    void testDownloadDocument_Officer_Success() throws Exception {
        byte[] content = "PDF bytes data".getBytes();
        when(documentService.getDocumentEntity(1L)).thenReturn(sampleEntity);
        when(documentService.loadDocumentContent(1L)).thenReturn(content);

        mockMvc.perform(get("/api/documents/1/download")
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(content));
    }

    @Test
    void testDownloadDocument_DifferentPassenger_Returns403() throws Exception {
        when(documentService.getDocumentEntity(1L))
                .thenThrow(new AccessDeniedException("You do not have permission to access this document"));

        mockMvc.perform(get("/api/documents/1/download")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testDownloadDocument_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/documents/1/download"))
                .andExpect(status().isUnauthorized());
    }
}
