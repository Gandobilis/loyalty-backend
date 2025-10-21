package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementDTO {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Integer totalPoints;
    private Integer eventCount;
    private Integer workingHours;
    private String mobileNumber;
    private Integer age;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
