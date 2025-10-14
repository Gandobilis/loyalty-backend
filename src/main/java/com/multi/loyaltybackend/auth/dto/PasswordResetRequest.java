package com.multi.loyaltybackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "Email cannot be empty for password reset")
        @Email(message = "Invalid email format")
        String email
) {
}