package com.multi.loyaltybackend.util;

import com.multi.loyaltybackend.dto.ApiResponse;
import com.multi.loyaltybackend.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Utility class for creating standardized API responses.
 * Provides convenient methods for building success and error responses
 * with consistent structure.
 */
public class ResponseUtil {

    private ResponseUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a success response with data.
     *
     * @param data The response data
     * @param <T> The type of data
     * @return ResponseEntity with standardized success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(data, null);
    }

    /**
     * Creates a success response with data and custom message.
     *
     * @param data The response data
     * @param message Custom success message
     * @param <T> The type of data
     * @return ResponseEntity with standardized success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    /**
     * Creates a success response with data, message, and custom HTTP status.
     *
     * @param data The response data
     * @param message Custom success message
     * @param status HTTP status code
     * @param <T> The type of data
     * @return ResponseEntity with standardized success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(getCurrentRequestPath())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Creates a created (201) response with data.
     *
     * @param data The created resource
     * @param <T> The type of data
     * @return ResponseEntity with standardized created response
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return created(data, "Resource created successfully");
    }

    /**
     * Creates a created (201) response with data and message.
     *
     * @param data The created resource
     * @param message Custom success message
     * @param <T> The type of data
     * @return ResponseEntity with standardized created response
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return success(data, message, HttpStatus.CREATED);
    }

    /**
     * Creates an accepted (202) response.
     *
     * @param message Message about the accepted request
     * @param <T> The type of data
     * @return ResponseEntity with standardized accepted response
     */
    public static <T> ResponseEntity<ApiResponse<T>> accepted(String message) {
        return success(null, message, HttpStatus.ACCEPTED);
    }

    /**
     * Creates a no content (204) response.
     *
     * @param <T> The type of data
     * @return ResponseEntity with no content
     */
    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates an error response.
     *
     * @param errorCode The error code
     * @param errorType The error type/title
     * @param message The error message
     * @param status HTTP status code
     * @param <T> The type of data
     * @return ResponseEntity with standardized error response
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(
            String errorCode,
            String errorType,
            String message,
            HttpStatus status
    ) {
        return error(errorCode, errorType, message, null, status);
    }

    /**
     * Creates an error response with context.
     *
     * @param errorCode The error code
     * @param errorType The error type/title
     * @param message The error message
     * @param context Additional error context
     * @param status HTTP status code
     * @param <T> The type of data
     * @return ResponseEntity with standardized error response
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(
            String errorCode,
            String errorType,
            String message,
            Object context,
            HttpStatus status
    ) {
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(errorCode, errorType, message);
        errorDetails.setContext(context);

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(getCurrentRequestPath())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Creates a bad request (400) error response.
     *
     * @param message The error message
     * @param <T> The type of data
     * @return ResponseEntity with bad request error
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return error("BAD_REQUEST", "Bad Request", message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates an unauthorized (401) error response.
     *
     * @param message The error message
     * @param <T> The type of data
     * @return ResponseEntity with unauthorized error
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return error("UNAUTHORIZED", "Unauthorized", message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Creates a forbidden (403) error response.
     *
     * @param message The error message
     * @param <T> The type of data
     * @return ResponseEntity with forbidden error
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return error("FORBIDDEN", "Forbidden", message, HttpStatus.FORBIDDEN);
    }

    /**
     * Creates a not found (404) error response.
     *
     * @param message The error message
     * @param <T> The type of data
     * @return ResponseEntity with not found error
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return error("NOT_FOUND", "Not Found", message, HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a conflict (409) error response.
     *
     * @param message The error message
     * @param <T> The type of data
     * @return ResponseEntity with conflict error
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return error("CONFLICT", "Conflict", message, HttpStatus.CONFLICT);
    }

    /**
     * Creates an internal server error (500) response.
     *
     * @param message The error message
     * @param <T> The type of data
     * @return ResponseEntity with internal server error
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return error("INTERNAL_ERROR", "Internal Server Error", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets the current request path from the request context.
     *
     * @return The request path, or null if not available
     */
    private static String getCurrentRequestPath() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            // Ignore - return null
        }
        return null;
    }
}
