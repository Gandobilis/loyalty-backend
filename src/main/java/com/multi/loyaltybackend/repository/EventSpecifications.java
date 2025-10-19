package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Event;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class EventSpecifications {

    public static Specification<Event> hasCategory(String category) {
        return (root, query, builder) -> {
            if (category == null) {
                return null;
            }
            return builder.equal(root.get("category"), category);
        };
    }

    public static Specification<Event> isBetweenDates(LocalDate startDate, LocalDate endDate) {
        return (root, query, builder) -> {
            if (startDate == null || endDate == null) {
                return null;
            }

            return builder.between(root.get("dateTime"), startDate, endDate);
        };
    }
}