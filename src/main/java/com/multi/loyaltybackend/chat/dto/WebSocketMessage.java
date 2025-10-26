package com.multi.loyaltybackend.chat.dto;

import com.multi.loyaltybackend.chat.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket real-time messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    private String type; // "MESSAGE", "TYPING", "READ", "JOINED", "LEFT"
    private Long chatId;
    private Long messageId;
    private Long senderId;
    private String senderFullName;
    private String content;
    private MessageType messageType;
    private String attachmentUrl;
    private LocalDateTime timestamp;
    private Object metadata;
}
