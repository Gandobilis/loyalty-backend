package com.multi.loyaltybackend.exception;

import com.multi.loyaltybackend.dto.ApiResponse;
import com.multi.loyaltybackend.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling with:
 * - Comprehensive logging with correlation IDs
 * - Consistent error response format (ApiResponse structure)
 * - Error code mapping
 * - Environment-aware debugging information
 * - Request context tracking
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.debug.include-stack-trace:false}")
    private boolean includeStackTrace;

    /**
     * Handles custom BaseException and its subclasses.
     * Provides comprehensive error handling with error codes and context.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, ex.getHttpStatus());

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ex.getErrorCode().getCode(),
                ex.getHttpStatus().getReasonPhrase(),
                ex.getMessage()
        );
        errorDetails.setContext(ex.getContext().isEmpty() ? null : ex.getContext());
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns detailed field-level validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        // Collect field errors
        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.VALIDATION_FAILED.getCode(),
                "Validation Failed",
                errorMessage
        );
        errorDetails.setContext(Map.of("fieldErrors", validationErrors));
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles authentication failures.
     * Returns 401 Unauthorized for invalid credentials or missing users.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.UNAUTHORIZED);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.AUTHENTICATION_FAILED.getCode(),
                "Authentication Failed",
                "Invalid credentials or user not found"
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles resource not found errors.
     * Returns 404 Not Found with resource details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.NOT_FOUND);

        Map<String, Object> context = new HashMap<>();
        context.put("resourceName", ex.getResourceName());
        context.put("fieldName", ex.getFieldName());
        context.put("fieldValue", ex.getFieldValue());

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "Resource Not Found",
                ex.getMessage()
        );
        errorDetails.setContext(context);
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles insufficient points errors.
     * Returns 400 Bad Request for point-related business logic failures.
     */
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientPointsException(
            InsufficientPointsException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        Map<String, Object> context = new HashMap<>();
        context.put("requiredPoints", ex.getRequiredPoints());
        context.put("availablePoints", ex.getAvailablePoints());

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.INSUFFICIENT_POINTS.getCode(),
                "Insufficient Points",
                ex.getMessage()
        );
        errorDetails.setContext(context);
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles password reset token errors.
     * Returns 400 Bad Request for invalid or expired tokens.
     */
    @ExceptionHandler({InvalidPasswordResetTokenException.class, PasswordResetTokenExpiredException.class})
    public ResponseEntity<ApiResponse<Object>> handlePasswordResetTokenException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ErrorCode errorCode = ex instanceof InvalidPasswordResetTokenException
                ? ErrorCode.INVALID_PASSWORD_RESET_TOKEN
                : ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED;

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                errorCode.getCode(),
                "Invalid Token",
                ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles duplicate resource conflicts.
     * Returns 409 Conflict for email duplication or voucher exchange conflicts.
     */
    @ExceptionHandler({EmailAlreadyExistsException.class, VoucherAlreadyExchangedException.class, DuplicateRegistrationException.class})
    public ResponseEntity<ApiResponse<Object>> handleConflictException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.CONFLICT);

        ErrorCode errorCode;
        if (ex instanceof EmailAlreadyExistsException) {
            errorCode = ErrorCode.EMAIL_ALREADY_EXISTS;
        } else if (ex instanceof VoucherAlreadyExchangedException) {
            errorCode = ErrorCode.VOUCHER_ALREADY_EXCHANGED;
        } else {
            errorCode = ErrorCode.DUPLICATE_REGISTRATION;
        }

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                errorCode.getCode(),
                "Resource Conflict",
                ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles voucher expiration errors.
     * Returns 400 Bad Request for expired vouchers.
     */
    @ExceptionHandler(VoucherExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleVoucherExpiredException(
            VoucherExpiredException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.VOUCHER_EXPIRED.getCode(),
                "Voucher Expired",
                ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles file storage errors.
     * Returns 500 Internal Server Error for file operations.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.FILE_STORAGE_ERROR.getCode(),
                "File Storage Error",
                ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handles invalid file path errors.
     * Returns 400 Bad Request for invalid file paths.
     */
    @ExceptionHandler(InvalidFilePathException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFilePathException(
            InvalidFilePathException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.INVALID_FILE_PATH.getCode(),
                "Invalid File Path",
                ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles illegal argument exceptions.
     * Returns 400 Bad Request for invalid input.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.INVALID_INPUT.getCode(),
                "Bad Request",
                ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles runtime exceptions.
     * Returns 500 Internal Server Error for unexpected runtime errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "Runtime Error",
                "A runtime error occurred: " + ex.getMessage()
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Global exception handler for all uncaught exceptions.
     * Returns 500 Internal Server Error for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "Internal Server Error",
                "An unexpected error occurred"
        );
        errorDetails.setStackTrace(includeStackTrace ? getStackTrace(ex) : null);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Logs exception details with context.
     *
     * @param ex The exception to log
     * @param request The HTTP request
     * @param status The HTTP status
     */
    private void logException(Exception ex, HttpServletRequest request, HttpStatus status) {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String errorCode = "UNKNOWN";

        if (ex instanceof BaseException baseEx) {
            errorCode = baseEx.getErrorCode().getCode();
        }

        if (status.is5xxServerError()) {
            log.error(
                    "Server error occurred - Method: {}, Path: {}, Status: {}, ErrorCode: {}, CorrelationId: {}, Message: {}",
                    method, path, status.value(), errorCode, correlationId, ex.getMessage()
            );
        } else if (status.is4xxClientError()) {
            log.warn(
                    "Client error occurred - Method: {}, Path: {}, Status: {}, ErrorCode: {}, CorrelationId: {}, Message: {}",
                    method, path, status.value(), errorCode, correlationId, ex.getMessage()
            );
        } else {
            log.info(
                    "Exception occurred - Method: {}, Path: {}, Status: {}, ErrorCode: {}, CorrelationId: {}, Message: {}",
                    method, path, status.value(), errorCode, correlationId, ex.getMessage()
            );
        }
    }

    /**
     * Converts exception stack trace to string.
     *
     * @param ex The exception
     * @return Stack trace as string
     */
    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
