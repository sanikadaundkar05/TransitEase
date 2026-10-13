package com.transitease.service;

import com.transitease.dto.AdminSummaryReportResponse;
import com.transitease.dto.CategoryReportResponse;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.PassCategory;
import com.transitease.entity.PassStatus;
import com.transitease.entity.Role;
import com.transitease.entity.User;
import com.transitease.entity.PassApplication;
import com.transitease.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PassApplicationRepository applicationRepository;

    @Mock
    private DigitalPassRepository digitalPassRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private PassCategoryRepository categoryRepository;

    @InjectMocks
    private AdminReportService reportService;

    private PassCategory sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new PassCategory();
        sampleCategory.setId(1L);
        sampleCategory.setName("Student Pass");
        sampleCategory.setBaseFare(25.0);
        sampleCategory.setValidityDays(30);
    }

    @Test
    void testGetSummaryReport_AggregatesCountsCorrectly() {
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByRole(Role.PASSENGER)).thenReturn(40L);
        when(userRepository.countByRole(Role.DOCUMENT_VERIFICATION_OFFICER)).thenReturn(4L);
        when(userRepository.countByRole(Role.PASS_APPROVAL_OFFICER)).thenReturn(3L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(3L);

        when(applicationRepository.count()).thenReturn(30L);
        when(applicationRepository.countByStatus(ApplicationStatus.PENDING)).thenReturn(10L);
        when(applicationRepository.countByStatus(ApplicationStatus.VERIFIED)).thenReturn(5L);
        when(applicationRepository.countByStatus(ApplicationStatus.APPROVED)).thenReturn(12L);
        when(applicationRepository.countByStatus(ApplicationStatus.REJECTED)).thenReturn(3L);
        when(applicationRepository.countByIsRenewal(true)).thenReturn(7L);

        when(digitalPassRepository.count()).thenReturn(12L);
        when(digitalPassRepository.countByStatus(PassStatus.ACTIVE)).thenReturn(10L);
        when(digitalPassRepository.countByStatus(PassStatus.EXPIRED)).thenReturn(2L);
        when(digitalPassRepository.countByStatus(PassStatus.SUSPENDED)).thenReturn(0L);

        when(routeRepository.count()).thenReturn(8L);
        when(routeRepository.countByActive(true)).thenReturn(7L);
        when(categoryRepository.count()).thenReturn(3L);

        AdminSummaryReportResponse response = reportService.getSummaryReport();

        assertNotNull(response);
        assertEquals(50L, response.getTotalUsers());
        assertEquals(40L, response.getTotalPassengers());
        assertEquals(7L, response.getTotalOfficers());
        assertEquals(3L, response.getTotalAdmins());
        assertEquals(30L, response.getTotalApplications());
        assertEquals(10L, response.getPendingApplications());
        assertEquals(12L, response.getApprovedApplications());
        assertEquals(7L, response.getRenewalApplications());
        assertEquals(10L, response.getActiveDigitalPasses());
        assertEquals(8L, response.getTotalRoutes());
        assertEquals(7L, response.getActiveRoutes());
        assertEquals(3L, response.getTotalPassCategories());
    }

    @Test
    void testGetCategoryBreakdown_ComputesRevenueCorrectly() {
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategory));
        when(applicationRepository.countByPassCategoryId(1L)).thenReturn(20L);
        when(applicationRepository.countByPassCategoryIdAndStatus(1L, ApplicationStatus.APPROVED)).thenReturn(10L);
        when(digitalPassRepository.countByApplicationPassCategoryId(1L)).thenReturn(10L);

        List<CategoryReportResponse> breakdown = reportService.getCategoryBreakdown();

        assertNotNull(breakdown);
        assertEquals(1, breakdown.size());
        CategoryReportResponse item = breakdown.get(0);
        assertEquals(1L, item.getCategoryId());
        assertEquals("Student Pass", item.getCategoryName());
        assertEquals(20L, item.getTotalApplications());
        assertEquals(10L, item.getApprovedApplications());
        assertEquals(10L, item.getActivePasses());
        assertEquals(250.0, item.getTotalEstimatedRevenue()); // 10 approved * 25.0
    }

    @Test
    void testGetApplicationStatusDistribution_CalculatesPercentages() {
        when(applicationRepository.count()).thenReturn(100L);
        when(applicationRepository.countByStatus(ApplicationStatus.PENDING)).thenReturn(20L);
        when(applicationRepository.countByStatus(ApplicationStatus.VERIFIED)).thenReturn(10L);
        when(applicationRepository.countByStatus(ApplicationStatus.APPROVED)).thenReturn(60L);
        when(applicationRepository.countByStatus(ApplicationStatus.REJECTED)).thenReturn(10L);

        Map<String, Object> map = reportService.getApplicationStatusDistribution();

        assertNotNull(map);
        assertEquals(100L, map.get("total"));
        assertEquals(60.0, map.get("approvalRatePercent"));
        assertEquals(10.0, map.get("rejectionRatePercent"));
    }

    @Test
    void testGetRecentApplications_ReturnsMappedList() {
        User passenger = new User();
        passenger.setId(1L);
        passenger.setFullName("Alice");
        passenger.setEmail("alice@test.com");

        PassApplication app = new PassApplication();
        app.setId(101L);
        app.setPassenger(passenger);
        app.setPassCategory(sampleCategory);
        app.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findTop10ByOrderByApplicationDateDesc()).thenReturn(List.of(app));

        List<PassApplicationResponse> recent = reportService.getRecentApplications();

        assertNotNull(recent);
        assertEquals(1, recent.size());
        assertEquals(101L, recent.get(0).getId());
    }
}
