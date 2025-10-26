package com.multi.loyaltybackend.chat.repository;

import com.multi.loyaltybackend.chat.model.Chat;
import com.multi.loyaltybackend.chat.model.ChatMessage;
import com.multi.loyaltybackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing ChatMessage entities.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages for a specific chat, paginated
     */
    Page<ChatMessage> findByChatOrderByCreatedAtAsc(Chat chat, Pageable pageable);

    /**
     * Find all messages for a specific chat
     */
    List<ChatMessage> findByChatOrderByCreatedAtAsc(Chat chat);

    /**
     * Find unread messages in a chat
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chat = :chat AND m.isRead = false ORDER BY m.createdAt ASC")
    List<ChatMessage> findUnreadMessagesByChat(@Param("chat") Chat chat);

    /**
     * Find unread messages for a user in a chat
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chat = :chat AND m.sender <> :user AND m.isRead = false ORDER BY m.createdAt ASC")
    List<ChatMessage> findUnreadMessagesForUser(@Param("chat") Chat chat, @Param("user") User user);

    /**
     * Count unread messages in a chat
     */
    long countByChatAndIsReadFalse(Chat chat);

    /**
     * Count unread messages for a user in a chat
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chat = :chat AND m.sender <> :user AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("chat") Chat chat, @Param("user") User user);

    /**
     * Mark all messages as read for a user in a chat
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.chat = :chat AND m.sender <> :user AND m.isRead = false")
    void markAllMessagesAsReadForUser(@Param("chat") Chat chat, @Param("user") User user);

    /**
     * Find last message in a chat
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chat = :chat ORDER BY m.createdAt DESC LIMIT 1")
    ChatMessage findLastMessageByChat(@Param("chat") Chat chat);

    /**
     * Delete all messages for a chat
     */
    void deleteByChat(Chat chat);

    /**
     * Count total messages in a chat
     */
    long countByChat(Chat chat);
}
