package com.multi.loyaltybackend.specification;

import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class UserSpecifications {

    public static Specification<User> hasRole(String role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null || role.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("role"), Role.valueOf(role.toUpperCase()));
        };
    }

    public static Specification<User> emailContains(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> fullNameContains(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasPointsGreaterThanOrEqual(Integer minPoints) {
        return (root, query, criteriaBuilder) -> {
            if (minPoints == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("totalPoints"), minPoints);
        };
    }

    public static Specification<User> hasPointsLessThanOrEqual(Integer maxPoints) {
        return (root, query, criteriaBuilder) -> {
            if (maxPoints == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("totalPoints"), maxPoints);
        };
    }

    public static Specification<User> createdAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
        };
    }

    public static Specification<User> createdBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }
}
