package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents the request payload to initiate a password reset process.
 *
 * @param email The email address of the user requesting the password reset.
 *              Must be a valid email format and cannot be blank.
 */
public record PasswordResetRequest(
        @NotBlank(message = "Email cannot be blank.")
        @Email(message = "Please provide a valid email address.")
        String email
) {
}