package com.multi.loyaltybackend.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("User with email already exists: " + email);
    }

    public EmailAlreadyExistsException(String message, boolean custom) {
        super(message);
    }
}
