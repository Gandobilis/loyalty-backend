package com.multi.loyaltybackend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class ProfileResponse {
    private String fullName;
    private Integer age;
    private String mobileNumber;
    private String aboutMe;
    private String fileName;
    private Integer totalPoints;
    private Integer eventCount;
    private Integer workingHours;
    private LocalDateTime createdAt;
}
