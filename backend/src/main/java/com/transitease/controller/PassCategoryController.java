package com.transitease.controller;

import com.transitease.dto.PassCategoryRequest;
import com.transitease.dto.PassCategoryResponse;
import com.transitease.service.PassCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pass-categories")
public class PassCategoryController {

    private final PassCategoryService passCategoryService;

    public PassCategoryController(PassCategoryService passCategoryService) {
        this.passCategoryService = passCategoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ROUTE_FARE_MANAGER')")
    public ResponseEntity<PassCategoryResponse> createPassCategory(@Valid @RequestBody PassCategoryRequest request) {
        PassCategoryResponse response = passCategoryService.createPassCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PassCategoryResponse>> getAllPassCategories() {
        List<PassCategoryResponse> responses = passCategoryService.getAllPassCategories();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassCategoryResponse> getPassCategoryById(@PathVariable Long id) {
        PassCategoryResponse response = passCategoryService.getPassCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROUTE_FARE_MANAGER')")
    public ResponseEntity<PassCategoryResponse> updatePassCategory(@PathVariable Long id,
                                                                   @Valid @RequestBody PassCategoryRequest request) {
        PassCategoryResponse response = passCategoryService.updatePassCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROUTE_FARE_MANAGER')")
    public ResponseEntity<Void> deletePassCategory(@PathVariable Long id) {
        passCategoryService.deletePassCategory(id);
        return ResponseEntity.noContent().build();
    }
}
