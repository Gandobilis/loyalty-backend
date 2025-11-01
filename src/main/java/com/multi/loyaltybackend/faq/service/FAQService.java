package com.multi.loyaltybackend.faq.service;

import com.multi.loyaltybackend.faq.dto.FAQFilterDTO;
import com.multi.loyaltybackend.faq.dto.FAQRequestDTO;
import com.multi.loyaltybackend.faq.dto.FAQResponseDTO;
import com.multi.loyaltybackend.faq.model.FAQ;
import com.multi.loyaltybackend.faq.repository.FAQRepository;
import com.multi.loyaltybackend.faq.specification.FAQSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing FAQs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FAQService {

    private final FAQRepository faqRepository;

    /**
     * Create a new FAQ (Admin)
     */
    @Transactional
    public FAQResponseDTO createFAQ(FAQRequestDTO requestDTO) {
        log.info("Creating new FAQ with category: {}", requestDTO.getCategory());

        FAQ faq = FAQ.builder()
                .category(requestDTO.getCategory())
                .question(requestDTO.getQuestion())
                .answer(requestDTO.getAnswer())
                .publish(requestDTO.getPublish())
                .popular(requestDTO.getPopular())
                .build();

        FAQ savedFAQ = faqRepository.save(faq);
        log.info("FAQ created successfully with ID: {}", savedFAQ.getId());

        return mapToResponseDTO(savedFAQ);
    }

    /**
     * Update an existing FAQ (Admin)
     */
    @Transactional
    public FAQResponseDTO updateFAQ(Long id, FAQRequestDTO requestDTO) {
        log.info("Updating FAQ with ID: {}", id);

        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("FAQ not found with ID: {}", id);
                    return new RuntimeException("FAQ not found with ID: " + id);
                });

        faq.setCategory(requestDTO.getCategory());
        faq.setQuestion(requestDTO.getQuestion());
        faq.setAnswer(requestDTO.getAnswer());
        faq.setPublish(requestDTO.getPublish());
        faq.setPopular(requestDTO.getPopular());

        FAQ updatedFAQ = faqRepository.save(faq);
        log.info("FAQ updated successfully with ID: {}", updatedFAQ.getId());

        return mapToResponseDTO(updatedFAQ);
    }

    /**
     * Delete a FAQ (Admin)
     */
    @Transactional
    public void deleteFAQ(Long id) {
        log.info("Deleting FAQ with ID: {}", id);

        if (!faqRepository.existsById(id)) {
            log.error("FAQ not found with ID: {}", id);
            throw new RuntimeException("FAQ not found with ID: " + id);
        }

        faqRepository.deleteById(id);
        log.info("FAQ deleted successfully with ID: {}", id);
    }

    /**
     * Get FAQ by ID (Admin)
     */
    @Transactional(readOnly = true)
    public FAQResponseDTO getFAQById(Long id) {
        log.info("Fetching FAQ with ID: {}", id);

        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("FAQ not found with ID: {}", id);
                    return new RuntimeException("FAQ not found with ID: " + id);
                });

        return mapToResponseDTO(faq);
    }

    /**
     * Get all FAQs with pagination and filtering (Admin)
     */
    @Transactional(readOnly = true)
    public Page<FAQResponseDTO> getAllFAQs(FAQFilterDTO filter, Pageable pageable) {
        log.info("Fetching all FAQs with filter: {}", filter);

        Specification<FAQ> spec = FAQSpecifications.filterFAQs(filter);
        Page<FAQ> faqPage = faqRepository.findAll(spec, pageable);

        log.info("Found {} FAQs", faqPage.getTotalElements());

        return faqPage.map(this::mapToResponseDTO);
    }

    /**
     * Get all FAQs without pagination (Admin)
     */
    @Transactional(readOnly = true)
    public List<FAQResponseDTO> getAllFAQsList(FAQFilterDTO filter) {
        log.info("Fetching all FAQs list with filter: {}", filter);

        Specification<FAQ> spec = FAQSpecifications.filterFAQs(filter);
        List<FAQ> faqs = faqRepository.findAll(spec);

        log.info("Found {} FAQs", faqs.size());

        return faqs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get published FAQs with pagination and filtering (User)
     */
    @Transactional(readOnly = true)
    public Page<FAQResponseDTO> getPublishedFAQs(FAQFilterDTO filter, Pageable pageable) {
        log.info("Fetching published FAQs with filter: {}", filter);

        // Ensure only published FAQs are returned
        filter.setPublish(true);

        Specification<FAQ> spec = FAQSpecifications.filterFAQs(filter);
        Page<FAQ> faqPage = faqRepository.findAll(spec, pageable);

        log.info("Found {} published FAQs", faqPage.getTotalElements());

        return faqPage.map(this::mapToResponseDTO);
    }

    /**
     * Get published FAQs without pagination (User)
     */
    @Transactional(readOnly = true)
    public List<FAQResponseDTO> getPublishedFAQsList(FAQFilterDTO filter) {
        log.info("Fetching published FAQs list with filter: {}", filter);

        // Ensure only published FAQs are returned
        filter.setPublish(true);

        Specification<FAQ> spec = FAQSpecifications.filterFAQs(filter);
        List<FAQ> faqs = faqRepository.findAll(spec);

        log.info("Found {} published FAQs", faqs.size());

        return faqs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all distinct categories (User & Admin)
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories(boolean publishedOnly) {
        log.info("Fetching all categories (published only: {})", publishedOnly);

        List<String> categories;
        if (publishedOnly) {
            categories = faqRepository.findDistinctCategoriesFromPublished();
        } else {
            categories = faqRepository.findDistinctCategories();
        }

        log.info("Found {} categories", categories.size());

        return categories;
    }

    /**
     * Toggle publish status (Admin)
     */
    @Transactional
    public FAQResponseDTO togglePublishStatus(Long id) {
        log.info("Toggling publish status for FAQ with ID: {}", id);

        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("FAQ not found with ID: {}", id);
                    return new RuntimeException("FAQ not found with ID: " + id);
                });

        faq.setPublish(!faq.getPublish());
        FAQ updatedFAQ = faqRepository.save(faq);

        log.info("FAQ publish status toggled to: {} for ID: {}", updatedFAQ.getPublish(), id);

        return mapToResponseDTO(updatedFAQ);
    }

    /**
     * Toggle popular status (Admin)
     */
    @Transactional
    public FAQResponseDTO togglePopularStatus(Long id) {
        log.info("Toggling popular status for FAQ with ID: {}", id);

        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("FAQ not found with ID: {}", id);
                    return new RuntimeException("FAQ not found with ID: " + id);
                });

        faq.setPopular(!faq.getPopular());
        FAQ updatedFAQ = faqRepository.save(faq);

        log.info("FAQ popular status toggled to: {} for ID: {}", updatedFAQ.getPopular(), id);

        return mapToResponseDTO(updatedFAQ);
    }

    /**
     * Map FAQ entity to response DTO
     */
    private FAQResponseDTO mapToResponseDTO(FAQ faq) {
        return FAQResponseDTO.builder()
                .id(faq.getId())
                .category(faq.getCategory())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .publish(faq.getPublish())
                .popular(faq.getPopular())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
