package com.transitease.repository;

import com.transitease.entity.ApplicationStatus;
import com.transitease.entity.PassApplication;
import com.transitease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PassApplicationRepository extends JpaRepository<PassApplication, Long> {

    List<PassApplication> findByPassenger(User passenger);

    List<PassApplication> findByPassengerOrderByApplicationDateDesc(User passenger);

    boolean existsByRenewedPassIdAndStatusIn(Long passId, Collection<ApplicationStatus> statuses);

    List<PassApplication> findByRenewedPassId(Long passId);

    long countByStatus(ApplicationStatus status);

    long countByIsRenewal(Boolean isRenewal);

    long countByPassCategoryId(Long passCategoryId);

    long countByPassCategoryIdAndStatus(Long passCategoryId, ApplicationStatus status);

    List<PassApplication> findTop10ByOrderByApplicationDateDesc();
}
