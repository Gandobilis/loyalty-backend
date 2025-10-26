package com.multi.loyaltybackend.faq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for FAQ response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FAQResponseDTO {

    private Long id;
    private String category;
    private String question;
    private String answer;
    private Boolean publish;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
