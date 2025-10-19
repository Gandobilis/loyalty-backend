package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
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
    private List<EventDTO> registrations;
}
