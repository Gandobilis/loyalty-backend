package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.EventCategory;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventSpecifications {

    public static Specification<Event> hasCategory(String category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null || category.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category"), EventCategory.valueOf(category.toUpperCase()));
        };
    }

    public static Specification<Event> titleContains(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Event> addressContains(String address) {
        return (root, query, criteriaBuilder) -> {
            if (address == null || address.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%");
        };
    }

    public static Specification<Event> dateTimeAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("dateTime"), startDate);
        };
    }

    public static Specification<Event> dateTimeBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dateTime"), endDate);
        };
    }

    public static Specification<Event> hasPointsGreaterThanOrEqual(Integer minPoints) {
        return (root, query, criteriaBuilder) -> {
            if (minPoints == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("points"), minPoints);
        };
    }

    public static Specification<Event> hasPointsLessThanOrEqual(Integer maxPoints) {
        return (root, query, criteriaBuilder) -> {
            if (maxPoints == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("points"), maxPoints);
        };
    }

    public static Specification<Event> hasLocation() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("latitude")),
                criteriaBuilder.isNotNull(root.get("longitude"))
            );
    }
}