package com.transitease.repository;

import com.transitease.entity.PassCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassCategoryRepository extends JpaRepository<PassCategory, Long> {

    Optional<PassCategory> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
