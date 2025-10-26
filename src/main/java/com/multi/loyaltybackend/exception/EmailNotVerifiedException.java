package com.multi.loyaltybackend.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email is not verified. Please verify your email to login.");
    }

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
