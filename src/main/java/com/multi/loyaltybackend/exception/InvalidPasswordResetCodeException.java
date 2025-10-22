package com.multi.loyaltybackend.exception;

public class InvalidPasswordResetCodeException extends RuntimeException {
    public InvalidPasswordResetCodeException() {
        super("Invalid password reset code.");
    }

    public InvalidPasswordResetCodeException(String message) {
        super(message);
    }
}
