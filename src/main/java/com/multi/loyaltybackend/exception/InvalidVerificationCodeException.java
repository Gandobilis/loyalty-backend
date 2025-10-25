package com.multi.loyaltybackend.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("Invalid email verification code.");
    }

    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}
