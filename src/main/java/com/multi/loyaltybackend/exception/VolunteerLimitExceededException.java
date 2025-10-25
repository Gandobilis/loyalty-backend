package com.multi.loyaltybackend.exception;

public class VolunteerLimitExceededException extends RuntimeException {
    public VolunteerLimitExceededException(String message) {
        super(message);
    }

    public VolunteerLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
