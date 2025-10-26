package com.multi.loyaltybackend.chat.dto;

import com.multi.loyaltybackend.chat.model.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering chats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFilterDTO {

    private ChatStatus status;
    private Long userId;
    private Long assignedToId;
    private String searchQuery;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private Boolean hasUnreadMessages;
}
