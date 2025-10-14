package com.multi.loyaltybackend.registration.exception;

public class DuplicateRegistrationException extends RuntimeException {

    public DuplicateRegistrationException(String message) {
        super(message);
    }
}