package com.multi.loyaltybackend.voucher.specification;

import com.multi.loyaltybackend.voucher.model.Voucher;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class VoucherSpecifications {

    public static Specification<Voucher> titleContains(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Voucher> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("company").get("id"), companyId);
        };
    }

    public static Specification<Voucher> hasPointsGreaterThanOrEqual(Integer minPoints) {
        return (root, query, criteriaBuilder) -> {
            if (minPoints == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("points"), minPoints);
        };
    }

    public static Specification<Voucher> hasPointsLessThanOrEqual(Integer maxPoints) {
        return (root, query, criteriaBuilder) -> {
            if (maxPoints == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("points"), maxPoints);
        };
    }

    public static Specification<Voucher> expiresAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("expiry"), startDate);
        };
    }

    public static Specification<Voucher> expiresBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("expiry"), endDate);
        };
    }

    public static Specification<Voucher> isActive() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("expiry"), LocalDateTime.now());
    }

    public static Specification<Voucher> isExpired() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThanOrEqualTo(root.get("expiry"), LocalDateTime.now());
    }

    public static Specification<Voucher> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            LocalDateTime now = LocalDateTime.now();
            if ("active".equalsIgnoreCase(status)) {
                return criteriaBuilder.greaterThan(root.get("expiry"), now);
            } else if ("expired".equalsIgnoreCase(status)) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("expiry"), now);
            }
            return criteriaBuilder.conjunction();
        };
    }
}
