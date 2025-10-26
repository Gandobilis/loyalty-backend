package com.multi.loyaltybackend.chat.exception;

import com.multi.loyaltybackend.exception.BaseException;
import com.multi.loyaltybackend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to perform operations on a closed chat.
 */
public class ChatAlreadyClosedException extends BaseException {

    public ChatAlreadyClosedException(Long chatId) {
        super(
            "Chat is already closed. Chat ID: " + chatId,
            HttpStatus.BAD_REQUEST,
            ErrorCode.INVALID_STATE
        );
        addContext("chatId", chatId);
    }

    public ChatAlreadyClosedException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.INVALID_STATE);
    }
}
