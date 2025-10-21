package com.multi.loyaltybackend.exception;

public class PasswordResetTokenExpiredException extends RuntimeException {
    public PasswordResetTokenExpiredException() {
        super("Password reset token has expired.");
    }

    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }
}
