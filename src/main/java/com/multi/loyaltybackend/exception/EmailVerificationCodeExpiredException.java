package com.multi.loyaltybackend.exception;

public class EmailVerificationCodeExpiredException extends RuntimeException {
    public EmailVerificationCodeExpiredException() {
        super("Email verification code has expired.");
    }

    public EmailVerificationCodeExpiredException(String message) {
        super(message);
    }
}
