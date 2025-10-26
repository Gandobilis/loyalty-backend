package com.multi.loyaltybackend.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning a chat to a support agent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAssignRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;
}
