package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.SupportMessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String response;
    private Long respondedByUserId;
    private String respondedByFullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime respondedAt;
}
