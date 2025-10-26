package com.multi.loyaltybackend.chat.exception;

import com.multi.loyaltybackend.exception.BaseException;
import com.multi.loyaltybackend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user tries to access a chat they don't have permission for.
 */
public class ChatAccessDeniedException extends BaseException {

    public ChatAccessDeniedException(Long userId, Long chatId) {
        super(
            "User does not have permission to access this chat",
            HttpStatus.FORBIDDEN,
            ErrorCode.ACCESS_DENIED
        );
        addContext("userId", userId);
        addContext("chatId", chatId);
    }

    public ChatAccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED);
    }
}
