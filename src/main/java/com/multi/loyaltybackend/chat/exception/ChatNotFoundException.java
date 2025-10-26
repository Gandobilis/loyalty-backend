package com.multi.loyaltybackend.chat.exception;

import com.multi.loyaltybackend.exception.BaseException;
import com.multi.loyaltybackend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a chat is not found.
 */
public class ChatNotFoundException extends BaseException {

    public ChatNotFoundException(Long chatId) {
        super(
            "Chat not found with ID: " + chatId,
            HttpStatus.NOT_FOUND,
            ErrorCode.RESOURCE_NOT_FOUND
        );
        addContext("chatId", chatId);
    }

    public ChatNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
