package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid password reset token is provided.
 */
public class InvalidPasswordResetTokenException extends BaseException {

    public InvalidPasswordResetTokenException() {
        super(ErrorCode.INVALID_PASSWORD_RESET_TOKEN, HttpStatus.BAD_REQUEST, "Invalid password reset token.");
    }

    public InvalidPasswordResetTokenException(String message) {
        super(ErrorCode.INVALID_PASSWORD_RESET_TOKEN, HttpStatus.BAD_REQUEST, message);
    }
}
