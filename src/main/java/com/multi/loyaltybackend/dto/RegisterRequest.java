package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,

        Role role,

        @NotBlank(message = "Full name cannot be empty")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password
) {
    public RegisterRequest {
        if (role == null) {
            role = Role.USER;
        }
    }
}