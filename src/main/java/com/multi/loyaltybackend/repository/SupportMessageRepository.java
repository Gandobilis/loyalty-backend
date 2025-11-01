package com.multi.loyaltybackend.repository;

import com.multi.loyaltybackend.model.SupportMessage;
import com.multi.loyaltybackend.model.SupportMessageStatus;
import com.multi.loyaltybackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    // Find all messages by a specific user with pagination
    Page<SupportMessage> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find all messages by status with pagination (for admin)
    Page<SupportMessage> findByStatusOrderByCreatedAtDesc(SupportMessageStatus status, Pageable pageable);

    // Find all messages ordered by creation date (for admin)
    Page<SupportMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Count messages by status
    long countByStatus(SupportMessageStatus status);
}
