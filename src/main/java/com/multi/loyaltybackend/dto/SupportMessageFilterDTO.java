package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.SupportMessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessageFilterDTO {
    private String userEmail;
    private String userFullName;
    private String subject;
    private SupportMessageStatus status;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
