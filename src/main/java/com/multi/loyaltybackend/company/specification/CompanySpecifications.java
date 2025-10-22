package com.multi.loyaltybackend.company.specification;

import com.multi.loyaltybackend.company.model.Company;
import org.springframework.data.jpa.domain.Specification;

public class CompanySpecifications {

    public static Specification<Company> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
}
