package com.multi.loyaltybackend.chat.dto;

import com.multi.loyaltybackend.chat.model.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for chat response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long assignedToId;
    private String assignedToFullName;
    private String subject;
    private ChatStatus status;
    private Integer messageCount;
    private Integer unreadByUser;
    private Integer unreadByAdmin;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
