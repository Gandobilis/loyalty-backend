package com.multi.loyaltybackend.chat.dto;

import com.multi.loyaltybackend.chat.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending a new message in a chat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequest {

    @NotNull(message = "Chat ID is required")
    private Long chatId;

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * S3 key if message has attachment (set by backend)
     */
    private String attachmentS3Key;

    private String attachmentFilename;

    private Long attachmentSize;

    private String attachmentMimeType;
}
