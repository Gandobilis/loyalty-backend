package com.multi.loyaltybackend.faq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering FAQs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FAQFilterDTO {

    private String category;
    private String searchQuery; // Search in question and answer
    private Boolean publish;
}
