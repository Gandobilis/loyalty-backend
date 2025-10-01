package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the authentication request payload.
 * <p>
 * This record includes validation for email and password fields and masks the
 * password in its string representation to prevent accidental logging.
 *
 * @param email    The user's email address. Must be a well-formed email and not blank.
 * @param password The user's password. Must meet complexity and length requirements.
 */
public record AuthRequest(
        @NotBlank(message = "Email cannot be blank.")
        @Email(message = "Please provide a valid email address.")
        String email,

        @NotBlank(message = "Password cannot be blank.")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters long.")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character."
        )
        String password
) {
    /**
     * Overrides the default toString() method to prevent the password field
     * from being exposed in logs or other string representations.
     *
     * @return A string representation of the object with the password masked.
     */
    @Override
    @NotNull
    public String toString() {
        return "AuthRequest[" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ']';
    }
}