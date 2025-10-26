package com.multi.loyaltybackend.chat.dto;

import com.multi.loyaltybackend.chat.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for message response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderFullName;
    private String senderEmail;
    private String content;
    private MessageType messageType;
    private String attachmentS3Key;
    private String attachmentFilename;
    private Long attachmentSize;
    private String attachmentMimeType;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Boolean isAdminMessage;
    private LocalDateTime createdAt;
}
