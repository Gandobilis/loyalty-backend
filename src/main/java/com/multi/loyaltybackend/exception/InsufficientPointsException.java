package com.multi.loyaltybackend.exception;

public class InsufficientPointsException extends RuntimeException {
    public InsufficientPointsException(int required, int available) {
        super("Insufficient points. Required: " + required + ", Available: " + available);
    }

    public InsufficientPointsException(String message) {
        super(message);
    }
}
