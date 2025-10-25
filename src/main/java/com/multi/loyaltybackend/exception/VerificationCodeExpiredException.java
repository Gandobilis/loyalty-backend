package com.multi.loyaltybackend.exception;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException() {
        super("Email verification code has expired. Please request a new code.");
    }

    public VerificationCodeExpiredException(String message) {
        super(message);
    }
}
