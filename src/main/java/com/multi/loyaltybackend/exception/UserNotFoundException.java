package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested user is not found.
 */
@Getter
public class UserNotFoundException extends BaseException {

    private final Long userId;

    public UserNotFoundException(Long id) {
        super(
                ErrorCode.USER_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("User not found with id: %d", id)
        );
        this.userId = id;
        addContext("userId", id);
    }

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, message);
        this.userId = null;
    }
}
