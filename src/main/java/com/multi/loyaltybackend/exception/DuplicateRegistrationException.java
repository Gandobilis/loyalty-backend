package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register for an event twice.
 */
public class DuplicateRegistrationException extends BaseException {

    public DuplicateRegistrationException(String message) {
        super(ErrorCode.DUPLICATE_REGISTRATION, HttpStatus.CONFLICT, message);
    }
}
