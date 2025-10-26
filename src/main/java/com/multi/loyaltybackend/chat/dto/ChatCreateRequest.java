package com.multi.loyaltybackend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new support chat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateRequest {

    @NotBlank(message = "Subject is required")
    @Size(min = 3, max = 200, message = "Subject must be between 3 and 200 characters")
    private String subject;

    @NotBlank(message = "Initial message is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String initialMessage;
}
