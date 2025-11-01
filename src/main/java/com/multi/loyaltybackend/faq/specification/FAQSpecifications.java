package com.multi.loyaltybackend.faq.specification;

import com.multi.loyaltybackend.faq.dto.FAQFilterDTO;
import com.multi.loyaltybackend.faq.model.FAQ;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifications for FAQ filtering
 */
public class FAQSpecifications {

    /**
     * Build specification from filter DTO
     */
    public static Specification<FAQ> filterFAQs(FAQFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by category
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category")),
                        filter.getCategory().toLowerCase()
                ));
            }

            // Filter by publish status
            if (filter.getPublish() != null) {
                predicates.add(criteriaBuilder.equal(root.get("publish"), filter.getPublish()));
            }

            // Filter by popular status
            if (filter.getPopular() != null) {
                predicates.add(criteriaBuilder.equal(root.get("popular"), filter.getPopular()));
            }

            // Search in question and answer
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isBlank()) {
                String searchPattern = "%" + filter.getSearchQuery().toLowerCase() + "%";
                Predicate questionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("question")),
                        searchPattern
                );
                Predicate answerMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("answer")),
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(questionMatch, answerMatch));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification for published FAQs only
     */
    public static Specification<FAQ> isPublished() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("publish"), true);
    }

    /**
     * Specification for popular FAQs only
     */
    public static Specification<FAQ> isPopular() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("popular"), true);
    }

    /**
     * Specification for FAQs by category
     */
    public static Specification<FAQ> hasCategory(String category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("category")),
                        category.toLowerCase()
                );
    }

    /**
     * Specification for search in question and answer
     */
    public static Specification<FAQ> searchInQuestionOrAnswer(String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";
            Predicate questionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("question")),
                    searchPattern
            );
            Predicate answerMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("answer")),
                    searchPattern
            );
            return criteriaBuilder.or(questionMatch, answerMatch);
        };
    }
}
