package com.transitease.controller;

import com.transitease.dto.PassCategoryRequest;
import com.transitease.dto.PassCategoryResponse;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.PassCategoryService;
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
class PassCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PassCategoryService passCategoryService;

    @MockitoBean
    private UserRepository userRepository;

    private String adminToken;
    private String managerToken;
    private String passengerToken;
    private PassCategoryResponse sampleCategory;

    @BeforeEach
    void setUp() {
        User admin = new User();
        admin.setId(1L);
        admin.setFullName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        User manager = new User();
        manager.setId(2L);
        manager.setFullName("Route Fare Manager");
        manager.setEmail("manager@example.com");
        manager.setRole(Role.ROUTE_FARE_MANAGER);

        User passenger = new User();
        passenger.setId(3L);
        passenger.setFullName("Passenger User");
        passenger.setEmail("passenger@example.com");
        passenger.setRole(Role.PASSENGER);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.of(manager));
        when(userRepository.findByEmail("passenger@example.com")).thenReturn(Optional.of(passenger));

        adminToken = jwtService.generateToken(admin);
        managerToken = jwtService.generateToken(manager);
        passengerToken = jwtService.generateToken(passenger);

        sampleCategory = new PassCategoryResponse(1L, "Student Monthly", "30 days student pass", 30, 20.0);
    }

    @Test
    void testGetAllPassCategories_Public_Success() throws Exception {
        when(passCategoryService.getAllPassCategories()).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/pass-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Student Monthly"))
                .andExpect(jsonPath("$[0].baseFare").value(20.0));
    }

    @Test
    void testGetPassCategoryById_Public_Success() throws Exception {
        when(passCategoryService.getPassCategoryById(1L)).thenReturn(sampleCategory);

        mockMvc.perform(get("/api/pass-categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Student Monthly"))
                .andExpect(jsonPath("$.validityDays").value(30));
    }

    @Test
    void testCreatePassCategory_AsAdmin_Success() throws Exception {
        when(passCategoryService.createPassCategory(any(PassCategoryRequest.class))).thenReturn(sampleCategory);

        String jsonBody = """
                {
                    "name": "Student Monthly",
                    "description": "30 days student pass",
                    "validityDays": 30,
                    "baseFare": 20.0
                }
                """;

        mockMvc.perform(post("/api/pass-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Student Monthly"));
    }

    @Test
    void testCreatePassCategory_AsRouteFareManager_Success() throws Exception {
        when(passCategoryService.createPassCategory(any(PassCategoryRequest.class))).thenReturn(sampleCategory);

        String jsonBody = """
                {
                    "name": "Student Monthly",
                    "description": "30 days student pass",
                    "validityDays": 30,
                    "baseFare": 20.0
                }
                """;

        mockMvc.perform(post("/api/pass-categories")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Student Monthly"));
    }

    @Test
    void testUpdatePassCategory_AsAdmin_Success() throws Exception {
        when(passCategoryService.updatePassCategory(eq(1L), any(PassCategoryRequest.class))).thenReturn(sampleCategory);

        String jsonBody = """
                {
                    "name": "Student Monthly",
                    "description": "Updated description",
                    "validityDays": 30,
                    "baseFare": 25.0
                }
                """;

        mockMvc.perform(put("/api/pass-categories/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Student Monthly"));
    }

    @Test
    void testDeletePassCategory_AsAdmin_Success() throws Exception {
        doNothing().when(passCategoryService).deletePassCategory(1L);

        mockMvc.perform(delete("/api/pass-categories/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreatePassCategory_AsPassenger_ReturnsForbidden() throws Exception {
        String jsonBody = """
                {
                    "name": "Student Monthly",
                    "validityDays": 30,
                    "baseFare": 20.0
                }
                """;

        mockMvc.perform(post("/api/pass-categories")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden());
    }
}
