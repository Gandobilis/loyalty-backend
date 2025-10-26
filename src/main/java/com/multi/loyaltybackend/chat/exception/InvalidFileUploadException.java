package com.multi.loyaltybackend.chat.exception;

import com.multi.loyaltybackend.exception.BaseException;
import com.multi.loyaltybackend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when file upload validation fails.
 */
public class InvalidFileUploadException extends BaseException {

    public InvalidFileUploadException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
    }

    public InvalidFileUploadException(String message, String filename) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR);
        addContext("filename", filename);
    }
}
