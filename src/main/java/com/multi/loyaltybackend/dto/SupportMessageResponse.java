package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.SupportMessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userMobileNumber;
    private String subject;
    private String message;
    private SupportMessageStatus status;
    @Builder.Default
    private List<SupportMessageResponseDTO> responses = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
