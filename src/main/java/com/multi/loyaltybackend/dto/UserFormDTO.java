package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.Role;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 150, message = "Age cannot exceed 150")
    private Integer age;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format. Must be E.164 format")
    private String mobileNumber;

    @Min(value = 0, message = "Points must be at least 0")
    private Integer totalPoints;
    @Min(value = 0, message = "Event number must be at least 0")
    private Integer eventCount;
    @Min(value = 0, message = "Hour must be at least 0")
    private Integer workingHours;
    @Size(max = 2000, message = "About me cannot exceed 2000 characters")
    private String aboutMe;
    private Role role;
}
