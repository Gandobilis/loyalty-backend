package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank(message = "Token cannot be empty")
        String token,

        @NotBlank(message = "New password cannot be empty")
        @Size(min = 8, message = "New password must be at least 8 characters long")
        String newPassword
) {
}