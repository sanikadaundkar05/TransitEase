package com.transitease.repository;

import com.transitease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<User> findByRole(com.transitease.entity.Role role);

    long countByRole(com.transitease.entity.Role role);
}