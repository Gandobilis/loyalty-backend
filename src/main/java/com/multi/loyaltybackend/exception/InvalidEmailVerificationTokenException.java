package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid email verification token is provided.
 */
public class InvalidEmailVerificationTokenException extends BaseException {

    public InvalidEmailVerificationTokenException() {
        super(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN, HttpStatus.BAD_REQUEST, "Invalid email verification token.");
    }

    public InvalidEmailVerificationTokenException(String message) {
        super(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN, HttpStatus.BAD_REQUEST, message);
    }
}
