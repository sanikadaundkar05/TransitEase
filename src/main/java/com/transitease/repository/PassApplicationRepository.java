package com.transitease.repository;

import com.transitease.entity.PassApplication;
import com.transitease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassApplicationRepository extends JpaRepository<PassApplication, Long> {
    List<PassApplication> findByPassenger(User passenger);
}
