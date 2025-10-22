package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetCodeRequest(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Reset code cannot be empty")
        @Size(min = 6, max = 6, message = "Reset code must be exactly 6 characters")
        @Pattern(regexp = "^[0-9]{6}$", message = "Reset code must be a 6-digit number")
        String code,

        @NotBlank(message = "New password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {
}
