package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFormDTO {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Integer age;
    private String mobileNumber;
    private Integer totalPoints;
    private Integer eventCount;
    private Integer workingHours;
    private String aboutMe;
    private Role role;
}
