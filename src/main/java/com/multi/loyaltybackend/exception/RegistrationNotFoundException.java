package com.multi.loyaltybackend.exception;

public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(String message) {
        super(message);
    }

    public RegistrationNotFoundException(Long id) {
        super("Registration not found with id: " + id);
    }
}
