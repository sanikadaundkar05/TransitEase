package com.transitease.controller;

import com.transitease.dto.PassApplicationResponse;
import com.transitease.dto.UpdateStatusRequest;
import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.PassApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PassApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PassApplicationService applicationService;

    @MockitoBean
    private com.transitease.service.DocumentService documentService;

    @MockitoBean
    private UserRepository userRepository;

    private String officerToken;
    private String passengerToken;

    @BeforeEach
    void setUp() {
        User officer = new User();
        officer.setId(2L);
        officer.setFullName("Approval Officer");
        officer.setEmail("officer@transitease.com");
        officer.setRole(Role.PASS_APPROVAL_OFFICER);

        User passenger = new User();
        passenger.setId(3L);
        passenger.setFullName("Passenger User");
        passenger.setEmail("passenger@transitease.com");
        passenger.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("officer@transitease.com")).thenReturn(Optional.of(officer));
        when(userRepository.findByEmail("passenger@transitease.com")).thenReturn(Optional.of(passenger));

        officerToken = jwtService.generateToken(officer);
        passengerToken = jwtService.generateToken(passenger);
    }

    @Test
    void testUpdateStatus_Verify_AsOfficer_Success() throws Exception {
        PassApplicationResponse response = new PassApplicationResponse();
        response.setId(10L);
        response.setStatus("VERIFIED");
        response.setRemarks("ID verified successfully");
        response.setRejectionReason("ID verified successfully");
        response.setApplicationDate(LocalDateTime.now());

        when(applicationService.updateApplicationStatus(eq(10L), eq(ApplicationStatus.VERIFIED), eq("ID verified successfully")))
                .thenReturn(response);

        mockMvc.perform(put("/api/pass-applications/10/status")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("status", "VERIFIED")
                        .param("remarks", "ID verified successfully"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.remarks").value("ID verified successfully"))
                .andExpect(jsonPath("$.rejectionReason").value("ID verified successfully"));
    }

    @Test
    void testUpdateStatus_Approve_AsOfficer_Success() throws Exception {
        PassApplicationResponse response = new PassApplicationResponse();
        response.setId(10L);
        response.setStatus("APPROVED");
        response.setRemarks("Approval granted");
        response.setApplicationDate(LocalDateTime.now());

        when(applicationService.updateApplicationStatus(eq(10L), eq(ApplicationStatus.APPROVED), eq("Approval granted")))
                .thenReturn(response);

        mockMvc.perform(put("/api/pass-applications/10/status")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("status", "APPROVED")
                        .param("remarks", "Approval granted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.remarks").value("Approval granted"));
    }

    @Test
    void testUpdateStatus_RejectWithRemarks_ViaRequestBody_Success() throws Exception {
        PassApplicationResponse response = new PassApplicationResponse();
        response.setId(10L);
        response.setStatus("REJECTED");
        response.setRemarks("Uploaded ID is expired");
        response.setRejectionReason("Uploaded ID is expired");
        response.setApplicationDate(LocalDateTime.now());

        when(applicationService.updateApplicationStatus(eq(10L), eq(ApplicationStatus.REJECTED), eq("Uploaded ID is expired")))
                .thenReturn(response);

        String jsonBody = """
                {
                    "status": "REJECTED",
                    "remarks": "Uploaded ID is expired"
                }
                """;

        mockMvc.perform(put("/api/pass-applications/10/status")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.remarks").value("Uploaded ID is expired"))
                .andExpect(jsonPath("$.rejectionReason").value("Uploaded ID is expired"));
    }

    @Test
    void testUpdateStatus_DirectApproval_ThrowsBadRequest() throws Exception {
        when(applicationService.updateApplicationStatus(eq(10L), eq(ApplicationStatus.APPROVED), any()))
                .thenThrow(new IllegalStateException("Cannot approve an application before document verification. Current status is PENDING."));

        mockMvc.perform(put("/api/pass-applications/10/status")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("status", "APPROVED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot approve an application before document verification. Current status is PENDING."));
    }

    @Test
    void testUpdateStatus_AsPassenger_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/api/pass-applications/10/status")
                        .header("Authorization", "Bearer " + passengerToken)
                        .param("status", "VERIFIED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateStatus_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/pass-applications/10/status")
                        .param("status", "VERIFIED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRenewPass_ViaApplicationsEndpoint_Success() throws Exception {
        PassApplicationResponse response = new PassApplicationResponse();
        response.setId(55L);
        response.setStatus("PENDING");
        response.setIsRenewal(true);
        response.setRenewedPassId(200L);

        when(applicationService.renewPass(eq(200L), any())).thenReturn(response);

        mockMvc.perform(post("/api/pass-applications/renew/200")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passCategoryId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.isRenewal").value(true))
                .andExpect(jsonPath("$.renewedPassId").value(200));
    }

    @Test
    void testGetMyHistory_ViaApplicationsEndpoint_Success() throws Exception {
        com.transitease.dto.PassengerHistoryResponse historyResponse =
                new com.transitease.dto.PassengerHistoryResponse(java.util.List.of(), java.util.List.of());
        when(applicationService.getPassengerHistory()).thenReturn(historyResponse);

        mockMvc.perform(get("/api/pass-applications/my-history")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applications").isArray())
                .andExpect(jsonPath("$.passes").isArray());
    }
}
