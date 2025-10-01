package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the request payload for registering a new user.
 *
 * @param username The desired username for the new account.
 * @param email    The user's email address, used for verification and communication.
 * @param password The user's desired password. Must meet complexity requirements.
 */
public record RegisterRequest(
        @NotBlank(message = "Username cannot be blank.")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
        String username,

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
     * Overrides the default toString() method to prevent the password
     * from being exposed in logs or other string representations.
     *
     * @return A string representation of the object with the password masked.
     */
    @Override
    @NotNull
    public String toString() {
        return "RegisterRequest[" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ']';
    }
}