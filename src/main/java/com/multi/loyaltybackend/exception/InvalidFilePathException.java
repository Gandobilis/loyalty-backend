package com.multi.loyaltybackend.exception;

public class InvalidFilePathException extends RuntimeException {
    public InvalidFilePathException() {
        super("Invalid file path");
    }

    public InvalidFilePathException(String message) {
        super(message);
    }
}
