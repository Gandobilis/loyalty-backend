package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register with an email that already exists.
 */
@Getter
public class EmailAlreadyExistsException extends BaseException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super(
                ErrorCode.EMAIL_ALREADY_EXISTS,
                HttpStatus.CONFLICT,
                String.format("User with email already exists: %s", email)
        );
        this.email = email;

        // Add context for debugging
        addContext("email", email);
    }

    public EmailAlreadyExistsException(String message, boolean custom) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT, message);
        this.email = null;
    }
}
