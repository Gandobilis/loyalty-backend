package com.multi.loyaltybackend.exception;

public class InvalidEmailVerificationCodeException extends RuntimeException {
    public InvalidEmailVerificationCodeException() {
        super("Invalid email verification code.");
    }

    public InvalidEmailVerificationCodeException(String message) {
        super(message);
    }
}
