package com.transitease.controller;

import com.transitease.dto.AdminSummaryReportResponse;
import com.transitease.dto.CategoryReportResponse;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.service.AdminReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService reportService;

    public AdminReportController(AdminReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryReportResponse> getSummaryReport() {
        return ResponseEntity.ok(reportService.getSummaryReport());
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<CategoryReportResponse>> getCategoryBreakdown() {
        return ResponseEntity.ok(reportService.getCategoryBreakdown());
    }

    @GetMapping("/applications-by-status")
    public ResponseEntity<Map<String, Object>> getApplicationStatusDistribution() {
        return ResponseEntity.ok(reportService.getApplicationStatusDistribution());
    }

    @GetMapping("/recent-applications")
    public ResponseEntity<List<PassApplicationResponse>> getRecentApplications() {
        return ResponseEntity.ok(reportService.getRecentApplications());
    }
}
