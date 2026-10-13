package com.transitease.repository;

import com.transitease.entity.DigitalPass;
import com.transitease.entity.PassApplication;
import com.transitease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalPassRepository extends JpaRepository<DigitalPass, Long> {

    List<DigitalPass> findByPassenger(User passenger);

    List<DigitalPass> findByPassengerOrderByIssueDateDesc(User passenger);

    List<DigitalPass> findByPassengerOrderByCreatedAtDesc(User passenger);

    List<DigitalPass> findByPassengerId(Long passengerId);

    Optional<DigitalPass> findByApplication(PassApplication application);

    Optional<DigitalPass> findByApplicationId(Long applicationId);

    Optional<DigitalPass> findByPassNumber(String passNumber);

    boolean existsByApplication(PassApplication application);

    boolean existsByPassNumber(String passNumber);

    long countByStatus(com.transitease.entity.PassStatus status);

    long countByApplicationPassCategoryId(Long passCategoryId);
}
