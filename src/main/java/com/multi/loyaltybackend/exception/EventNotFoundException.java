package com.multi.loyaltybackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested event is not found.
 */
@Getter
public class EventNotFoundException extends BaseException {

    private final Long eventId;

    public EventNotFoundException(Long id) {
        super(
                ErrorCode.EVENT_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("Event not found with id: %d", id)
        );
        this.eventId = id;
        addContext("eventId", id);
    }

    public EventNotFoundException(String message) {
        super(ErrorCode.EVENT_NOT_FOUND, HttpStatus.NOT_FOUND, message);
        this.eventId = null;
    }
}
