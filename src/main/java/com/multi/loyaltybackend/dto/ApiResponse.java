package com.multi.loyaltybackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all endpoints.
 * Provides consistent structure for both success and error responses.
 *
 * @param <T> The type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    private LocalDateTime timestamp;
    private String correlationId;
    private String path;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Error details for failed responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String errorCode;
        private String error;
        private String message;
        private Object context;
        private String stackTrace;

        public ErrorDetails() {}

        public ErrorDetails(String errorCode, String error, String message) {
            this.errorCode = errorCode;
            this.error = error;
            this.message = message;
        }

        // Getters and Setters
        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getContext() {
            return context;
        }

        public void setContext(Object context) {
            this.context = context;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Creates a builder for constructing ApiResponse instances.
     *
     * @param <T> The type of data
     * @return A new ApiResponseBuilder
     */
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    /**
     * Builder class for ApiResponse.
     * Provides a fluent interface for constructing responses.
     */
    public static class ApiResponseBuilder<T> {
        private final ApiResponse<T> response;

        public ApiResponseBuilder() {
            this.response = new ApiResponse<>();
        }

        public ApiResponseBuilder<T> success(boolean success) {
            response.setSuccess(success);
            return this;
        }

        public ApiResponseBuilder<T> message(String message) {
            response.setMessage(message);
            return this;
        }

        public ApiResponseBuilder<T> data(T data) {
            response.setData(data);
            return this;
        }

        public ApiResponseBuilder<T> error(ErrorDetails error) {
            response.setError(error);
            return this;
        }

        public ApiResponseBuilder<T> errorCode(String errorCode) {
            if (response.getError() == null) {
                response.setError(new ErrorDetails());
            }
            response.getError().setErrorCode(errorCode);
            return this;
        }

        public ApiResponseBuilder<T> errorMessage(String errorMessage) {
            if (response.getError() == null) {
                response.setError(new ErrorDetails());
            }
            response.getError().setMessage(errorMessage);
            return this;
        }

        public ApiResponseBuilder<T> errorType(String errorType) {
            if (response.getError() == null) {
                response.setError(new ErrorDetails());
            }
            response.getError().setError(errorType);
            return this;
        }

        public ApiResponseBuilder<T> errorContext(Object context) {
            if (response.getError() == null) {
                response.setError(new ErrorDetails());
            }
            response.getError().setContext(context);
            return this;
        }

        public ApiResponseBuilder<T> stackTrace(String stackTrace) {
            if (response.getError() == null) {
                response.setError(new ErrorDetails());
            }
            response.getError().setStackTrace(stackTrace);
            return this;
        }

        public ApiResponseBuilder<T> timestamp(LocalDateTime timestamp) {
            response.setTimestamp(timestamp);
            return this;
        }

        public ApiResponseBuilder<T> correlationId(String correlationId) {
            response.setCorrelationId(correlationId);
            return this;
        }

        public ApiResponseBuilder<T> path(String path) {
            response.setPath(path);
            return this;
        }

        public ApiResponse<T> build() {
            return response;
        }
    }
}
