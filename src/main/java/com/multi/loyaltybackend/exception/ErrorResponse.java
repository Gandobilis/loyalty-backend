package com.multi.loyaltybackend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enhanced error response structure for API errors.
 * Provides comprehensive error information including error codes,
 * correlation IDs, and optional debug information.
 *
 * The response includes:
 * - timestamp: When the error occurred
 * - status: HTTP status code
 * - errorCode: Programmatic error identifier
 * - error: Human-readable error type
 * - message: Detailed error message
 * - path: Request path that caused the error
 * - correlationId: Unique ID for tracking this request
 * - context: Additional context data (optional, debug only)
 * - stackTrace: Stack trace information (optional, debug only)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String error,
        String message,
        String path,
        String correlationId,
        Map<String, Object> context,
        String stackTrace
) {
    /**
     * Creates a builder for constructing ErrorResponse instances.
     *
     * @return A new ErrorResponseBuilder
     */
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    /**
     * Builder class for ErrorResponse.
     * Provides a fluent interface for constructing error responses.
     */
    public static class ErrorResponseBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private int status;
        private String errorCode;
        private String error;
        private String message;
        private String path;
        private String correlationId;
        private Map<String, Object> context;
        private String stackTrace;

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponseBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ErrorResponseBuilder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public ErrorResponseBuilder stackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(
                    timestamp,
                    status,
                    errorCode,
                    error,
                    message,
                    path,
                    correlationId,
                    context,
                    stackTrace
            );
        }
    }
}
