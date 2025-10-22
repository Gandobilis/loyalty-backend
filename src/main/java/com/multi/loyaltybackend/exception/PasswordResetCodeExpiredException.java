package com.multi.loyaltybackend.exception;

public class PasswordResetCodeExpiredException extends RuntimeException {
    public PasswordResetCodeExpiredException() {
        super("Password reset code has expired. Please request a new one.");
    }

    public PasswordResetCodeExpiredException(String message) {
        super(message);
    }
}
