package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationManagementDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Long eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventCategory;
    private LocalDateTime eventDateTime;
    private Integer eventPoints;
    private Integer eventVolunteerLimit;
    private String comment;
    private RegistrationStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
}
