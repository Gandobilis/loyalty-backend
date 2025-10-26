package com.multi.loyaltybackend.chat.model;

import com.multi.loyaltybackend.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a single message in a chat session.
 * Messages are stored both in PostgreSQL (for relational queries)
 * and DynamoDB (for scalability and fast retrieval).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_created", columnList = "chat_id, createdAt"),
    @Index(name = "idx_sender_created", columnList = "sender_id, createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Chat this message belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @NotNull(message = "Chat is required")
    private Chat chat;

    /**
     * User who sent the message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Sender is required")
    private User sender;

    /**
     * Message content
     */
    @Column(nullable = false, length = 5000)
    @NotNull(message = "Content is required")
    private String content;

    /**
     * Type of message
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * S3 file key if message has attachment
     */
    @Column(name = "attachment_s3_key")
    private String attachmentS3Key;

    /**
     * Original filename of attachment
     */
    @Column(name = "attachment_filename")
    private String attachmentFilename;

    /**
     * File size in bytes
     */
    @Column(name = "attachment_size")
    private Long attachmentSize;

    /**
     * MIME type of attachment
     */
    @Column(name = "attachment_mime_type")
    private String attachmentMimeType;

    /**
     * Whether the message has been read
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    /**
     * When the message was read
     */
    private LocalDateTime readAt;

    /**
     * Whether this message was sent by admin/support
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean isAdminMessage = false;

    /**
     * DynamoDB message ID (for sync reference)
     */
    @Column(name = "dynamodb_message_id")
    private String dynamoDbMessageId;

    /**
     * When the message was created
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Mark message as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
