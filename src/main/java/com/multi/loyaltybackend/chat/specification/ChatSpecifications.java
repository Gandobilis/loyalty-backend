package com.multi.loyaltybackend.chat.specification;

import com.multi.loyaltybackend.chat.dto.ChatFilterDTO;
import com.multi.loyaltybackend.chat.model.Chat;
import com.multi.loyaltybackend.chat.model.ChatStatus;
import com.multi.loyaltybackend.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for filtering Chat entities.
 */
public class ChatSpecifications {

    /**
     * Create a specification from filter DTO
     */
    public static Specification<Chat> withFilters(ChatFilterDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.getStatus()));
            }

            if (filters.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filters.getUserId()));
            }

            if (filters.getAssignedToId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo").get("id"), filters.getAssignedToId()));
            }

            if (filters.getSearchQuery() != null && !filters.getSearchQuery().isBlank()) {
                String searchPattern = "%" + filters.getSearchQuery().toLowerCase() + "%";
                Predicate subjectMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("subject")),
                    searchPattern
                );
                Predicate messageMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastMessagePreview")),
                    searchPattern
                );
                predicates.add(criteriaBuilder.or(subjectMatch, messageMatch));
            }

            if (filters.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    filters.getCreatedAfter()
                ));
            }

            if (filters.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    filters.getCreatedBefore()
                ));
            }

            if (filters.getHasUnreadMessages() != null && filters.getHasUnreadMessages()) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get("unreadByUser"), 0),
                    criteriaBuilder.greaterThan(root.get("unreadByAdmin"), 0)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter chats by user
     */
    public static Specification<Chat> byUser(User user) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user"), user);
    }

    /**
     * Filter chats by status
     */
    public static Specification<Chat> byStatus(ChatStatus status) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Filter chats assigned to an agent
     */
    public static Specification<Chat> byAssignedTo(User agent) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("assignedTo"), agent);
    }

    /**
     * Filter chats with unread messages for user
     */
    public static Specification<Chat> withUnreadForUser() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("unreadByUser"), 0);
    }

    /**
     * Filter chats with unread messages for admin
     */
    public static Specification<Chat> withUnreadForAdmin() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThan(root.get("unreadByAdmin"), 0);
    }
}
