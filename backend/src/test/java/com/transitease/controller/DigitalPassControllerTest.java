package com.transitease.controller;

import com.transitease.dto.DigitalPassResponse;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.DigitalPassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DigitalPassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private DigitalPassService digitalPassService;

    @MockitoBean
    private com.transitease.service.PassApplicationService applicationService;

    @MockitoBean
    private UserRepository userRepository;

    private String passengerToken;
    private DigitalPassResponse samplePass;

    @BeforeEach
    void setUp() {
        User passenger = new User();
        passenger.setId(10L);
        passenger.setFullName("Alice Smith");
        passenger.setEmail("alice@example.com");
        passenger.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(passenger));
        passengerToken = jwtService.generateToken(passenger);

        samplePass = new DigitalPassResponse();
        samplePass.setId(500L);
        samplePass.setPassNumber("TE-PASS-20261013-12345678");
        samplePass.setPassengerId(10L);
        samplePass.setPassengerName("Alice Smith");
        samplePass.setPassengerEmail("alice@example.com");
        samplePass.setPassCategoryName("Monthly Pass");
        samplePass.setIssueDate(LocalDate.now());
        samplePass.setValidityStartDate(LocalDate.now());
        samplePass.setValidityEndDate(LocalDate.now().plusDays(30));
        samplePass.setStatus("ACTIVE");
    }

    @Test
    void testGetMyPasses_Success() throws Exception {
        when(digitalPassService.getMyPasses()).thenReturn(List.of(samplePass));

        mockMvc.perform(get("/api/passes/my-passes")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(500))
                .andExpect(jsonPath("$[0].passNumber").value("TE-PASS-20261013-12345678"))
                .andExpect(jsonPath("$[0].passengerName").value("Alice Smith"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void testGetMyPasses_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/passes/my-passes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetPassById_Owner_Success() throws Exception {
        when(digitalPassService.getPassById(500L)).thenReturn(samplePass);

        mockMvc.perform(get("/api/passes/500")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(500))
                .andExpect(jsonPath("$.passNumber").value("TE-PASS-20261013-12345678"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetPassById_AnotherPassenger_Returns403() throws Exception {
        when(digitalPassService.getPassById(999L))
                .thenThrow(new AccessDeniedException("You do not have permission to access this pass"));

        mockMvc.perform(get("/api/passes/999")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this pass"));
    }

    @Test
    void testDownloadPassPdf_Owner_Success() throws Exception {
        byte[] fakePdfBytes = "%PDF-1.4\nTest PDF content\n%%EOF".getBytes();
        when(digitalPassService.getPassById(500L)).thenReturn(samplePass);
        when(digitalPassService.downloadPassPdf(500L)).thenReturn(fakePdfBytes);

        mockMvc.perform(get("/api/passes/500/download")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transit-pass-TE-PASS-20261013-12345678.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(fakePdfBytes));
    }

    @Test
    void testDownloadPassPdf_AnotherPassenger_Returns403() throws Exception {
        when(digitalPassService.getPassById(999L))
                .thenThrow(new AccessDeniedException("You do not have permission to access this pass"));

        mockMvc.perform(get("/api/passes/999/download")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testRenewPass_Success_Returns201() throws Exception {
        com.transitease.dto.PassApplicationResponse renewalResponse = new com.transitease.dto.PassApplicationResponse();
        renewalResponse.setId(105L);
        renewalResponse.setStatus("PENDING");
        renewalResponse.setIsRenewal(true);
        renewalResponse.setRenewedPassId(500L);

        when(applicationService.renewPass(eq(500L), any())).thenReturn(renewalResponse);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/passes/500/renew")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passCategoryId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(105))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.isRenewal").value(true))
                .andExpect(jsonPath("$.renewedPassId").value(500));
    }

    @Test
    void testRenewPass_UnauthorizedPassenger_Returns403() throws Exception {
        when(applicationService.renewPass(eq(500L), any()))
                .thenThrow(new AccessDeniedException("You cannot renew another passenger's pass"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/passes/500/renew")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void testRenewPass_DuplicatePending_Returns400() throws Exception {
        when(applicationService.renewPass(eq(500L), any()))
                .thenThrow(new IllegalStateException("A renewal application is already pending or under review for this pass"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/passes/500/renew")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A renewal application is already pending or under review for this pass"));
    }

    @Test
    void testGetMyHistory_Success() throws Exception {
        com.transitease.dto.PassengerHistoryResponse historyResponse =
                new com.transitease.dto.PassengerHistoryResponse(java.util.List.of(), java.util.List.of(samplePass));
        when(applicationService.getPassengerHistory()).thenReturn(historyResponse);

        mockMvc.perform(get("/api/passes/my-history")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passes[0].id").value(500))
                .andExpect(jsonPath("$.passes[0].passNumber").value("TE-PASS-20261013-12345678"));
    }
}
