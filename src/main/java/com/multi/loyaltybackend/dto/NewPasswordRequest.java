package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the request payload for setting a new password.
 *
 * @param token       The password reset token. Cannot be blank.
 * @param newPassword The user's desired new password. Must meet complexity and length requirements.
 */
public record NewPasswordRequest(
        @NotBlank(message = "Token cannot be blank.")
        String token,

        @NotBlank(message = "New password cannot be blank.")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters long.")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character."
        )
        String newPassword
) {
    /**
     * Overrides the default toString() method to prevent the new password
     * from being exposed in logs or other string representations.
     *
     * @return A string representation of the object with the password masked.
     */
    @Override
    @NotNull
    public String toString() {
        return "NewPasswordRequest[" +
                "token='" + token + '\'' +
                ", newPassword='[PROTECTED]'" +
                ']';
    }
}