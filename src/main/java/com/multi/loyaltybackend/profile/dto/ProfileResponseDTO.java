package com.multi.loyaltybackend.profile.dto;

import com.multi.loyaltybackend.auth.model.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private Integer age;
    private String mobileNumber;
    private String aboutMe;
    private String fileName;
    private Role role;
    private Integer totalPoints;
    private Integer eventCount;
    private Integer workingHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}