package com.multi.loyaltybackend.chat.repository;

import com.multi.loyaltybackend.chat.model.Chat;
import com.multi.loyaltybackend.chat.model.ChatStatus;
import com.multi.loyaltybackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Chat entities.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>, JpaSpecificationExecutor<Chat> {

    /**
     * Find all chats for a specific user
     */
    List<Chat> findByUserOrderByLastMessageAtDesc(User user);

    /**
     * Find all chats assigned to a specific agent
     */
    List<Chat> findByAssignedToOrderByLastMessageAtDesc(User assignedTo);

    /**
     * Find all chats with a specific status
     */
    List<Chat> findByStatusOrderByCreatedAtDesc(ChatStatus status);

    /**
     * Find all chats for a user with a specific status
     */
    List<Chat> findByUserAndStatusOrderByLastMessageAtDesc(User user, ChatStatus status);

    /**
     * Find chats with unread messages by user
     */
    @Query("SELECT c FROM Chat c WHERE c.user = :user AND c.unreadByUser > 0 ORDER BY c.lastMessageAt DESC")
    List<Chat> findChatsWithUnreadMessagesByUser(@Param("user") User user);

    /**
     * Find chats with unread messages for admin
     */
    @Query("SELECT c FROM Chat c WHERE c.unreadByAdmin > 0 ORDER BY c.lastMessageAt DESC")
    List<Chat> findChatsWithUnreadMessagesForAdmin();

    /**
     * Count open chats for a user
     */
    long countByUserAndStatus(User user, ChatStatus status);

    /**
     * Count total open chats
     */
    long countByStatus(ChatStatus status);

    /**
     * Find chat by ID and user (for access control)
     */
    Optional<Chat> findByIdAndUser(Long id, User user);

    /**
     * Search chats by subject
     */
    @Query("SELECT c FROM Chat c WHERE LOWER(c.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY c.lastMessageAt DESC")
    List<Chat> searchBySubject(@Param("searchTerm") String searchTerm);
}
