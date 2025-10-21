package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid file path is provided.
 */
public class InvalidFilePathException extends BaseException {

    public InvalidFilePathException() {
        super(ErrorCode.INVALID_FILE_PATH, HttpStatus.BAD_REQUEST, "Invalid file path");
    }

    public InvalidFilePathException(String message) {
        super(ErrorCode.INVALID_FILE_PATH, HttpStatus.BAD_REQUEST, message);
    }
}
