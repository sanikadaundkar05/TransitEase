package com.transitease.controller;

import com.transitease.dto.CreateStaffRequest;
import com.transitease.dto.UpdateUserRequest;
import com.transitease.dto.UserResponse;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private String adminToken;
    private String passengerToken;
    private UserResponse sampleUserResponse;

    @BeforeEach
    void setUp() {
        User admin = new User();
        admin.setId(1L);
        admin.setFullName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        User passenger = new User();
        passenger.setId(2L);
        passenger.setFullName("Passenger User");
        passenger.setEmail("passenger@example.com");
        passenger.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        adminToken = jwtService.generateToken(admin);
        passengerToken = jwtService.generateToken(passenger);

        sampleUserResponse = new UserResponse(10L, "Staff Officer", "officer@example.com", Role.DOCUMENT_VERIFICATION_OFFICER, "Success");
    }

    @Test
    void testGetAllUsers_AsAdmin_Success() throws Exception {
        when(userService.getAllUsers(null)).thenReturn(List.of(sampleUserResponse));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].fullName").value("Staff Officer"))
                .andExpect(jsonPath("$[0].role").value("DOCUMENT_VERIFICATION_OFFICER"));
    }

    @Test
    void testGetUserById_AsAdmin_Success() throws Exception {
        when(userService.getUserById(10L)).thenReturn(sampleUserResponse);

        mockMvc.perform(get("/api/admin/users/10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.fullName").value("Staff Officer"));
    }

    @Test
    void testCreateStaffUser_AsAdmin_Success() throws Exception {
        when(userService.createStaffUser(any(CreateStaffRequest.class))).thenReturn(sampleUserResponse);

        String jsonBody = """
                {
                    "fullName": "Staff Officer",
                    "email": "officer@example.com",
                    "password": "password123",
                    "role": "DOCUMENT_VERIFICATION_OFFICER"
                }
                """;

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.role").value("DOCUMENT_VERIFICATION_OFFICER"));
    }

    @Test
    void testUpdateUser_AsAdmin_Success() throws Exception {
        UserResponse updated = new UserResponse(10L, "Updated Officer", "officer@example.com", Role.PASS_APPROVAL_OFFICER, "Updated");
        when(userService.updateUser(eq(10L), any(UpdateUserRequest.class))).thenReturn(updated);

        String jsonBody = """
                {
                    "fullName": "Updated Officer",
                    "role": "PASS_APPROVAL_OFFICER"
                }
                """;

        mockMvc.perform(put("/api/admin/users/10")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Officer"))
                .andExpect(jsonPath("$.role").value("PASS_APPROVAL_OFFICER"));
    }

    @Test
    void testUpdateUserRole_AsAdmin_Success() throws Exception {
        UserResponse updated = new UserResponse(10L, "Staff Officer", "officer@example.com", Role.PASS_APPROVAL_OFFICER, "Updated");
        when(userService.updateUserRole(eq(10L), eq(Role.PASS_APPROVAL_OFFICER))).thenReturn(updated);

        mockMvc.perform(patch("/api/admin/users/10/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"PASS_APPROVAL_OFFICER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PASS_APPROVAL_OFFICER"));
    }

    @Test
    void testDeleteUser_AsAdmin_Success() throws Exception {
        doNothing().when(userService).deleteUser(10L);

        mockMvc.perform(delete("/api/admin/users/10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAdminEndpoints_AsPassenger_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminEndpoints_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
