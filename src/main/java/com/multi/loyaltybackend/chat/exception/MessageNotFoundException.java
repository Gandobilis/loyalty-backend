package com.multi.loyaltybackend.chat.exception;

import com.multi.loyaltybackend.exception.BaseException;
import com.multi.loyaltybackend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a message is not found.
 */
public class MessageNotFoundException extends BaseException {

    public MessageNotFoundException(Long messageId) {
        super(
            "Message not found with ID: " + messageId,
            HttpStatus.NOT_FOUND,
            ErrorCode.RESOURCE_NOT_FOUND
        );
        addContext("messageId", messageId);
    }

    public MessageNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
