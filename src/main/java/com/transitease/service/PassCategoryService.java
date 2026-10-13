package com.transitease.service;

import com.transitease.dto.PassCategoryRequest;
import com.transitease.dto.PassCategoryResponse;
import com.transitease.entity.PassCategory;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.PassCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassCategoryService {

    private final PassCategoryRepository passCategoryRepository;

    public PassCategoryService(PassCategoryRepository passCategoryRepository) {
        this.passCategoryRepository = passCategoryRepository;
    }

    @Transactional
    public PassCategoryResponse createPassCategory(PassCategoryRequest request) {
        if (passCategoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Pass category with name '" + request.getName() + "' already exists");
        }

        PassCategory passCategory = new PassCategory();
        passCategory.setName(request.getName());
        passCategory.setDescription(request.getDescription());
        passCategory.setValidityDays(request.getValidityDays());
        passCategory.setBaseFare(request.getBaseFare() != null ? request.getBaseFare() : 0.0);
        passCategory.setCategoryGroup(request.getCategoryGroup());
        passCategory.setEligibility(request.getEligibility());
        passCategory.setMandatoryDocuments(request.getMandatoryDocuments());
        passCategory.setOptionalDocuments(request.getOptionalDocuments());
        passCategory.setFareType(request.getFareType() != null ? request.getFareType() : "FIXED");
        passCategory.setDiscountDetails(request.getDiscountDetails());

        PassCategory saved = passCategoryRepository.save(passCategory);
        return PassCategoryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<PassCategoryResponse> getAllPassCategories() {
        return passCategoryRepository.findAll()
                .stream()
                .map(PassCategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PassCategoryResponse getPassCategoryById(Long id) {
        PassCategory passCategory = passCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pass category not found with ID: " + id));
        return PassCategoryResponse.fromEntity(passCategory);
    }

    @Transactional
    public PassCategoryResponse updatePassCategory(Long id, PassCategoryRequest request) {
        PassCategory passCategory = passCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pass category not found with ID: " + id));

        if (passCategoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new RuntimeException("Pass category with name '" + request.getName() + "' already exists");
        }

        passCategory.setName(request.getName());
        passCategory.setDescription(request.getDescription());
        passCategory.setValidityDays(request.getValidityDays());
        if (request.getBaseFare() != null) {
            passCategory.setBaseFare(request.getBaseFare());
        }
        if (request.getCategoryGroup() != null) {
            passCategory.setCategoryGroup(request.getCategoryGroup());
        }
        if (request.getEligibility() != null) {
            passCategory.setEligibility(request.getEligibility());
        }
        if (request.getMandatoryDocuments() != null) {
            passCategory.setMandatoryDocuments(request.getMandatoryDocuments());
        }
        if (request.getOptionalDocuments() != null) {
            passCategory.setOptionalDocuments(request.getOptionalDocuments());
        }
        if (request.getFareType() != null) {
            passCategory.setFareType(request.getFareType());
        }
        if (request.getDiscountDetails() != null) {
            passCategory.setDiscountDetails(request.getDiscountDetails());
        }

        PassCategory updated = passCategoryRepository.save(passCategory);
        return PassCategoryResponse.fromEntity(updated);
    }

    @Transactional
    public void deletePassCategory(Long id) {
        PassCategory passCategory = passCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pass category not found with ID: " + id));
        passCategoryRepository.delete(passCategory);
    }
}
