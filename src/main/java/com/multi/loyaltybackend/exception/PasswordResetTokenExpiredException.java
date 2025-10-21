package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a password reset token has expired.
 */
public class PasswordResetTokenExpiredException extends BaseException {

    public PasswordResetTokenExpiredException() {
        super(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED, HttpStatus.BAD_REQUEST, "Password reset token has expired.");
    }

    public PasswordResetTokenExpiredException(String message) {
        super(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED, HttpStatus.BAD_REQUEST, message);
    }
}
