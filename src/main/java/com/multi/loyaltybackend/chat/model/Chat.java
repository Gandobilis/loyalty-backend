package com.multi.loyaltybackend.chat.model;

import com.multi.loyaltybackend.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a support chat session between a user and support team.
 * Each chat can have multiple messages.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chats")
@EntityListeners(AuditingEntityListener.class)
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who initiated the chat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    /**
     * Admin/Support agent handling the chat (optional, assigned later)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    /**
     * Subject/title of the chat
     */
    @Column(nullable = false, length = 200)
    @NotNull(message = "Subject is required")
    private String subject;

    /**
     * Current status of the chat
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChatStatus status = ChatStatus.OPEN;

    /**
     * DynamoDB table name for messages (reference)
     */
    @Column(name = "dynamodb_table_reference")
    private String dynamoDbTableReference;

    /**
     * Number of messages in this chat (cached for performance)
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer messageCount = 0;

    /**
     * Number of unread messages by user
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer unreadByUser = 0;

    /**
     * Number of unread messages by admin
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer unreadByAdmin = 0;

    /**
     * Last message preview (cached for quick display)
     */
    @Column(length = 500)
    private String lastMessagePreview;

    /**
     * When the last message was sent
     */
    private LocalDateTime lastMessageAt;

    /**
     * When the chat was created
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the chat was last updated
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When the chat was closed (if applicable)
     */
    private LocalDateTime closedAt;

    /**
     * Messages in this chat (stored in PostgreSQL for relational queries)
     * For large scale, messages are also stored in DynamoDB
     */
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * Increment message count
     */
    public void incrementMessageCount() {
        this.messageCount++;
    }

    /**
     * Mark chat as closed
     */
    public void close() {
        this.status = ChatStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Reopen a closed chat
     */
    public void reopen() {
        if (this.status == ChatStatus.CLOSED) {
            this.status = ChatStatus.REOPENED;
            this.closedAt = null;
        }
    }
}
