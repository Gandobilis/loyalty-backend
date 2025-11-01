package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageResponseDTO {
    private Long id;
    private Long respondedByUserId;
    private String respondedByFullName;
    private String response;
    private LocalDateTime createdAt;
}
