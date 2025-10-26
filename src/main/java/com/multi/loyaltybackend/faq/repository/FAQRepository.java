package com.multi.loyaltybackend.faq.repository;

import com.multi.loyaltybackend.faq.model.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for FAQ entity
 */
@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long>, JpaSpecificationExecutor<FAQ> {

    /**
     * Find all published FAQs
     */
    List<FAQ> findByPublishTrue();

    /**
     * Find published FAQs by category
     */
    List<FAQ> findByCategoryAndPublishTrue(String category);

    /**
     * Find all FAQs by category (admin)
     */
    List<FAQ> findByCategory(String category);

    /**
     * Find all distinct categories
     */
    @Query("SELECT DISTINCT f.category FROM FAQ f ORDER BY f.category")
    List<String> findDistinctCategories();

    /**
     * Find distinct categories from published FAQs
     */
    @Query("SELECT DISTINCT f.category FROM FAQ f WHERE f.publish = true ORDER BY f.category")
    List<String> findDistinctCategoriesFromPublished();
}
