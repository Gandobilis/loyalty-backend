package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an email verification token has expired.
 */
public class EmailVerificationTokenExpiredException extends BaseException {

    public EmailVerificationTokenExpiredException() {
        super(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED, HttpStatus.BAD_REQUEST, "Email verification token has expired.");
    }

    public EmailVerificationTokenExpiredException(String message) {
        super(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED, HttpStatus.BAD_REQUEST, message);
    }
}
