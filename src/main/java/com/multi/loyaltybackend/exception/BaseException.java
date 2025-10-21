package com.multi.loyaltybackend.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all custom application exceptions.
 * Provides consistent structure for error handling including error codes,
 * HTTP status mapping, and additional context data.
 *
 * All custom exceptions should extend this class to ensure consistent
 * error handling and reporting across the application.
 */
public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> context;

    /**
     * Creates a new exception with error code and HTTP status.
     *
     * @param errorCode The error code identifying this exception type
     * @param httpStatus The HTTP status code to return
     * @param message The error message
     */
    protected BaseException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.context = new HashMap<>();
    }

    /**
     * Creates a new exception with error code, HTTP status, and cause.
     *
     * @param errorCode The error code identifying this exception type
     * @param httpStatus The HTTP status code to return
     * @param message The error message
     * @param cause The underlying cause of this exception
     */
    protected BaseException(ErrorCode errorCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.context = new HashMap<>();
    }

    /**
     * Adds contextual information to the exception.
     * This information can be used for logging and debugging.
     *
     * @param key The context key
     * @param value The context value
     * @return This exception for method chaining
     */
    public BaseException addContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Gets the error code for this exception.
     *
     * @return The error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the HTTP status code for this exception.
     *
     * @return The HTTP status
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Gets the context data for this exception.
     *
     * @return Unmodifiable map of context data
     */
    public Map<String, Object> getContext() {
        return Map.copyOf(context);
    }

    /**
     * Gets a string representation of the context data.
     *
     * @return Context as string, or empty string if no context
     */
    public String getContextAsString() {
        if (context.isEmpty()) {
            return "";
        }
        return context.toString();
    }
}
