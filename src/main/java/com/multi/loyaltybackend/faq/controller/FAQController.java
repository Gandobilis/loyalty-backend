package com.multi.loyaltybackend.faq.controller;

import com.multi.loyaltybackend.faq.dto.FAQFilterDTO;
import com.multi.loyaltybackend.faq.dto.FAQRequestDTO;
import com.multi.loyaltybackend.faq.dto.FAQResponseDTO;
import com.multi.loyaltybackend.faq.service.FAQService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for FAQ management
 * Provides endpoints for both admin (CRUD operations) and users (view published FAQs)
 */
@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FAQ", description = "FAQ management endpoints")
public class FAQController {

    @Autowired
    private final FAQService faqService;

    // ==================== USER ENDPOINTS ====================

    /**
     * Get all published FAQs with pagination and filtering
     * Public endpoint - no authentication required
     */
    @GetMapping
    @Operation(summary = "Get published FAQs", description = "Get all published FAQs with optional filtering")
    public ResponseEntity<Page<FAQResponseDTO>> getPublishedFAQs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Public request to get published FAQs - category: {}, search: {}, page: {}, size: {}",
                category, search, page, size);

        FAQFilterDTO filter = FAQFilterDTO.builder()
                .category(category)
                .searchQuery(search)
                .publish(true)
                .build();

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<FAQResponseDTO> faqs = faqService.getPublishedFAQs(filter, pageable);

        return ResponseEntity.ok(faqs);
    }

    /**
     * Get all published FAQs without pagination
     * Public endpoint - no authentication required
     */
    @GetMapping("/all")
    @Operation(summary = "Get all published FAQs", description = "Get all published FAQs without pagination")
    public ResponseEntity<List<FAQResponseDTO>> getAllPublishedFAQs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        log.info("Public request to get all published FAQs - category: {}, search: {}", category, search);

        FAQFilterDTO filter = FAQFilterDTO.builder()
                .category(category)
                .searchQuery(search)
                .publish(true)
                .build();

        List<FAQResponseDTO> faqs = faqService.getPublishedFAQsList(filter);

        return ResponseEntity.ok(faqs);
    }

    /**
     * Get all published categories
     * Public endpoint - no authentication required
     */
    @GetMapping("/categories")
    @Operation(summary = "Get published categories", description = "Get all distinct categories from published FAQs")
    public ResponseEntity<List<String>> getPublishedCategories() {
        log.info("Public request to get published FAQ categories");

        List<String> categories = faqService.getAllCategories(true);

        return ResponseEntity.ok(categories);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Create a new FAQ (Admin only)
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create FAQ", description = "Create a new FAQ (Admin only)")
    public ResponseEntity<FAQResponseDTO> createFAQ(@Valid @RequestBody FAQRequestDTO requestDTO) {
        log.info("Admin request to create FAQ");

        FAQResponseDTO faq = faqService.createFAQ(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(faq);
    }

    /**
     * Update an existing FAQ (Admin only)
     */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update FAQ", description = "Update an existing FAQ (Admin only)")
    public ResponseEntity<FAQResponseDTO> updateFAQ(
            @PathVariable Long id,
            @Valid @RequestBody FAQRequestDTO requestDTO
    ) {
        log.info("Admin request to update FAQ with ID: {}", id);

        FAQResponseDTO faq = faqService.updateFAQ(id, requestDTO);

        return ResponseEntity.ok(faq);
    }

    /**
     * Delete a FAQ (Admin only)
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete FAQ", description = "Delete a FAQ (Admin only)")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        log.info("Admin request to delete FAQ with ID: {}", id);

        faqService.deleteFAQ(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get FAQ by ID (Admin only)
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get FAQ by ID", description = "Get a FAQ by ID (Admin only)")
    public ResponseEntity<FAQResponseDTO> getFAQById(@PathVariable Long id) {
        log.info("Admin request to get FAQ with ID: {}", id);

        FAQResponseDTO faq = faqService.getFAQById(id);

        return ResponseEntity.ok(faq);
    }

    /**
     * Get all FAQs with pagination and filtering (Admin only)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all FAQs", description = "Get all FAQs with filtering (Admin only)")
    public ResponseEntity<Page<FAQResponseDTO>> getAllFAQs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean publish,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Admin request to get all FAQs - category: {}, search: {}, publish: {}, page: {}, size: {}",
                category, search, publish, page, size);

        FAQFilterDTO filter = FAQFilterDTO.builder()
                .category(category)
                .searchQuery(search)
                .publish(publish)
                .build();

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<FAQResponseDTO> faqs = faqService.getAllFAQs(filter, pageable);

        return ResponseEntity.ok(faqs);
    }

    /**
     * Get all FAQs without pagination (Admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all FAQs list", description = "Get all FAQs without pagination (Admin only)")
    public ResponseEntity<List<FAQResponseDTO>> getAllFAQsList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean publish
    ) {
        log.info("Admin request to get all FAQs list - category: {}, search: {}, publish: {}",
                category, search, publish);

        FAQFilterDTO filter = FAQFilterDTO.builder()
                .category(category)
                .searchQuery(search)
                .publish(publish)
                .build();

        List<FAQResponseDTO> faqs = faqService.getAllFAQsList(filter);

        return ResponseEntity.ok(faqs);
    }

    /**
     * Get all categories including unpublished (Admin only)
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all categories", description = "Get all distinct categories from all FAQs (Admin only)")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("Admin request to get all FAQ categories");

        List<String> categories = faqService.getAllCategories(false);

        return ResponseEntity.ok(categories);
    }

    /**
     * Toggle publish status of a FAQ (Admin only)
     */
    @PatchMapping("/admin/{id}/toggle-publish")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Toggle publish status", description = "Toggle the publish status of a FAQ (Admin only)")
    public ResponseEntity<FAQResponseDTO> togglePublishStatus(@PathVariable Long id) {
        log.info("Admin request to toggle publish status for FAQ with ID: {}", id);

        FAQResponseDTO faq = faqService.togglePublishStatus(id);

        return ResponseEntity.ok(faq);
    }
}
