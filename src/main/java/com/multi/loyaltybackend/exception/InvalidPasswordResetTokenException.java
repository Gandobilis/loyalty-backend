package com.multi.loyaltybackend.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException() {
        super("Invalid password reset token.");
    }

    public InvalidPasswordResetTokenException(String message) {
        super(message);
    }
}
