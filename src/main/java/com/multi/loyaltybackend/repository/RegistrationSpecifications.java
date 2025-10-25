package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.model.RegistrationStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class RegistrationSpecifications {

    public static Specification<Registration> userEmailContains(String email) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.join("user", JoinType.LEFT).get("email")),
                        "%" + email.toLowerCase() + "%");
    }

    public static Specification<Registration> userNameContains(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.join("user", JoinType.LEFT).get("fullName")),
                        "%" + name.toLowerCase() + "%");
    }

    public static Specification<Registration> eventTitleContains(String title) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.join("event", JoinType.LEFT).get("title")),
                        "%" + title.toLowerCase() + "%");
    }

    public static Specification<Registration> hasEventId(Long eventId) {
        return (root, query, cb) ->
                cb.equal(root.join("event", JoinType.LEFT).get("id"), eventId);
    }

    public static Specification<Registration> hasStatus(String status) {
        return (root, query, cb) -> {
            try {
                RegistrationStatus regStatus = RegistrationStatus.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), regStatus);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Registration> registeredAfter(LocalDateTime dateTime) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("registeredAt"), dateTime);
    }

    public static Specification<Registration> registeredBefore(LocalDateTime dateTime) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("registeredAt"), dateTime);
    }
}
