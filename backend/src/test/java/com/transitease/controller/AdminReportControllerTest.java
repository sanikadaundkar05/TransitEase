package com.transitease.controller;

import com.transitease.dto.AdminSummaryReportResponse;
import com.transitease.dto.CategoryReportResponse;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.repository.UserRepository;
import com.transitease.security.JwtService;
import com.transitease.service.AdminReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AdminReportService reportService;

    @MockitoBean
    private UserRepository userRepository;

    private String adminToken;
    private String passengerToken;

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
    }

    @Test
    void testGetSummaryReport_AsAdmin_Success() throws Exception {
        AdminSummaryReportResponse summary = new AdminSummaryReportResponse();
        summary.setTotalUsers(50);
        summary.setTotalPassengers(40);
        summary.setTotalApplications(30);
        summary.setApprovedApplications(20);
        summary.setActiveDigitalPasses(18);

        when(reportService.getSummaryReport()).thenReturn(summary);

        mockMvc.perform(get("/api/admin/reports/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(50))
                .andExpect(jsonPath("$.totalPassengers").value(40))
                .andExpect(jsonPath("$.approvedApplications").value(20))
                .andExpect(jsonPath("$.activeDigitalPasses").value(18));
    }

    @Test
    void testGetCategoryBreakdown_AsAdmin_Success() throws Exception {
        CategoryReportResponse cat = new CategoryReportResponse(1L, "Student Pass", 20.0, 30, 15, 10, 8, 200.0);
        when(reportService.getCategoryBreakdown()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/admin/reports/by-category")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Student Pass"))
                .andExpect(jsonPath("$[0].totalApplications").value(15))
                .andExpect(jsonPath("$[0].totalEstimatedRevenue").value(200.0));
    }

    @Test
    void testGetApplicationStatusDistribution_AsAdmin_Success() throws Exception {
        Map<String, Object> map = Map.of("total", 100L, "approved", 70L, "approvalRatePercent", 70.0);
        when(reportService.getApplicationStatusDistribution()).thenReturn(map);

        mockMvc.perform(get("/api/admin/reports/applications-by-status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.approvalRatePercent").value(70.0));
    }

    @Test
    void testGetRecentApplications_AsAdmin_Success() throws Exception {
        PassApplicationResponse app = new PassApplicationResponse();
        app.setId(101L);
        app.setStatus("PENDING");
        when(reportService.getRecentApplications()).thenReturn(List.of(app));

        mockMvc.perform(get("/api/admin/reports/recent-applications")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void testGetReports_AsPassenger_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/reports/summary")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden());
    }
}
