package com.transitease.repository;

import com.transitease.entity.ApplicationDocument;
import com.transitease.entity.PassApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findByApplication(PassApplication application);

    List<ApplicationDocument> findByApplicationId(Long applicationId);

    List<ApplicationDocument> findByUploadedById(Long userId);
}
