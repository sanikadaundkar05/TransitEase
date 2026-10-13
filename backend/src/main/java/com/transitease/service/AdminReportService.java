package com.transitease.service;

import com.transitease.dto.AdminSummaryReportResponse;
import com.transitease.dto.CategoryReportResponse;
import com.transitease.dto.PassApplicationResponse;
import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.PassCategory;
import com.transitease.entity.PassStatus;
import com.transitease.entity.Role;
import com.transitease.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminReportService {

    private final UserRepository userRepository;
    private final PassApplicationRepository applicationRepository;
    private final DigitalPassRepository digitalPassRepository;
    private final RouteRepository routeRepository;
    private final PassCategoryRepository categoryRepository;

    public AdminReportService(UserRepository userRepository,
                              PassApplicationRepository applicationRepository,
                              DigitalPassRepository digitalPassRepository,
                              RouteRepository routeRepository,
                              PassCategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.digitalPassRepository = digitalPassRepository;
        this.routeRepository = routeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public AdminSummaryReportResponse getSummaryReport() {
        AdminSummaryReportResponse summary = new AdminSummaryReportResponse();

        long totalUsers = userRepository.count();
        long totalPassengers = userRepository.countByRole(Role.PASSENGER);
        long totalVerifiers = userRepository.countByRole(Role.DOCUMENT_VERIFICATION_OFFICER);
        long totalApprovers = userRepository.countByRole(Role.PASS_APPROVAL_OFFICER);
        long totalAdmins = userRepository.countByRole(Role.ADMIN);

        summary.setTotalUsers(totalUsers);
        summary.setTotalPassengers(totalPassengers);
        summary.setTotalOfficers(totalVerifiers + totalApprovers);
        summary.setTotalAdmins(totalAdmins);

        summary.setTotalApplications(applicationRepository.count());
        summary.setPendingApplications(applicationRepository.countByStatus(ApplicationStatus.PENDING));
        summary.setVerifiedApplications(applicationRepository.countByStatus(ApplicationStatus.VERIFIED));
        summary.setApprovedApplications(applicationRepository.countByStatus(ApplicationStatus.APPROVED));
        summary.setRejectedApplications(applicationRepository.countByStatus(ApplicationStatus.REJECTED));
        summary.setRenewalApplications(applicationRepository.countByIsRenewal(true));

        summary.setTotalDigitalPasses(digitalPassRepository.count());
        summary.setActiveDigitalPasses(digitalPassRepository.countByStatus(PassStatus.ACTIVE));
        summary.setExpiredDigitalPasses(digitalPassRepository.countByStatus(PassStatus.EXPIRED));
        summary.setSuspendedDigitalPasses(digitalPassRepository.countByStatus(PassStatus.SUSPENDED));

        summary.setTotalRoutes(routeRepository.count());
        summary.setActiveRoutes(routeRepository.countByActive(true));
        summary.setTotalPassCategories(categoryRepository.count());

        return summary;
    }

    @Transactional(readOnly = true)
    public List<CategoryReportResponse> getCategoryBreakdown() {
        List<PassCategory> categories = categoryRepository.findAll();
        return categories.stream().map(cat -> {
            long totalApps = applicationRepository.countByPassCategoryId(cat.getId());
            long approvedApps = applicationRepository.countByPassCategoryIdAndStatus(cat.getId(), ApplicationStatus.APPROVED);
            long activePasses = digitalPassRepository.countByApplicationPassCategoryId(cat.getId());
            double baseFare = cat.getBaseFare() != null ? cat.getBaseFare() : 0.0;
            double revenue = approvedApps * baseFare;

            return new CategoryReportResponse(
                    cat.getId(),
                    cat.getName(),
                    cat.getBaseFare(),
                    cat.getValidityDays(),
                    totalApps,
                    approvedApps,
                    activePasses,
                    revenue
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getApplicationStatusDistribution() {
        long total = applicationRepository.count();
        long pending = applicationRepository.countByStatus(ApplicationStatus.PENDING);
        long verified = applicationRepository.countByStatus(ApplicationStatus.VERIFIED);
        long approved = applicationRepository.countByStatus(ApplicationStatus.APPROVED);
        long rejected = applicationRepository.countByStatus(ApplicationStatus.REJECTED);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", total);
        map.put("pending", pending);
        map.put("verified", verified);
        map.put("approved", approved);
        map.put("rejected", rejected);

        if (total > 0) {
            map.put("approvalRatePercent", Math.round(((double) approved / total) * 100.0 * 10.0) / 10.0);
            map.put("rejectionRatePercent", Math.round(((double) rejected / total) * 100.0 * 10.0) / 10.0);
        } else {
            map.put("approvalRatePercent", 0.0);
            map.put("rejectionRatePercent", 0.0);
        }

        return map;
    }

    @Transactional(readOnly = true)
    public List<PassApplicationResponse> getRecentApplications() {
        return applicationRepository.findTop10ByOrderByApplicationDateDesc().stream()
                .map(PassApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
