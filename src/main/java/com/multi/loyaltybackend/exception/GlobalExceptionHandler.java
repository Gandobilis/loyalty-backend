package com.multi.loyaltybackend.exception;

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
 * - Consistent error response format
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
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, ex.getHttpStatus());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus().value())
                .errorCode(ex.getErrorCode().getCode())
                .error(ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .context(ex.getContext().isEmpty() ? null : ex.getContext())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns detailed field-level validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
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

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VALIDATION_FAILED.getCode())
                .error("Validation Failed")
                .message(errorMessage)
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .context(Map.of("fieldErrors", validationErrors))
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles authentication failures.
     * Returns 401 Unauthorized for invalid credentials or missing users.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .errorCode(ErrorCode.AUTHENTICATION_FAILED.getCode())
                .error("Authentication Failed")
                .message("Invalid credentials or user not found")
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles resource not found errors.
     * Returns 404 Not Found with resource details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.NOT_FOUND);

        Map<String, Object> context = new HashMap<>();
        context.put("resourceName", ex.getResourceName());
        context.put("fieldName", ex.getFieldName());
        context.put("fieldValue", ex.getFieldValue());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .context(context)
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles insufficient points errors.
     * Returns 400 Bad Request for point-related business logic failures.
     */
    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPointsException(
            InsufficientPointsException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        Map<String, Object> context = new HashMap<>();
        context.put("requiredPoints", ex.getRequiredPoints());
        context.put("availablePoints", ex.getAvailablePoints());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.INSUFFICIENT_POINTS.getCode())
                .error("Insufficient Points")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .context(context)
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles password reset token errors.
     * Returns 400 Bad Request for invalid or expired tokens.
     */
    @ExceptionHandler({InvalidPasswordResetTokenException.class, PasswordResetTokenExpiredException.class})
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ErrorCode errorCode = ex instanceof InvalidPasswordResetTokenException
                ? ErrorCode.INVALID_PASSWORD_RESET_TOKEN
                : ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(errorCode.getCode())
                .error("Invalid Token")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles duplicate resource conflicts.
     * Returns 409 Conflict for email duplication or voucher exchange conflicts.
     */
    @ExceptionHandler({EmailAlreadyExistsException.class, VoucherAlreadyExchangedException.class, DuplicateRegistrationException.class})
    public ResponseEntity<ErrorResponse> handleConflictException(
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

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .errorCode(errorCode.getCode())
                .error("Resource Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles voucher expiration errors.
     * Returns 400 Bad Request for expired vouchers.
     */
    @ExceptionHandler(VoucherExpiredException.class)
    public ResponseEntity<ErrorResponse> handleVoucherExpiredException(
            VoucherExpiredException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VOUCHER_EXPIRED.getCode())
                .error("Voucher Expired")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles file storage errors.
     * Returns 500 Internal Server Error for file operations.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.FILE_STORAGE_ERROR.getCode())
                .error("File Storage Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles invalid file path errors.
     * Returns 400 Bad Request for invalid file paths.
     */
    @ExceptionHandler(InvalidFilePathException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFilePathException(
            InvalidFilePathException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.INVALID_FILE_PATH.getCode())
                .error("Invalid File Path")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles illegal argument exceptions.
     * Returns 400 Bad Request for invalid input.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.BAD_REQUEST);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.INVALID_INPUT.getCode())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles runtime exceptions.
     * Returns 500 Internal Server Error for unexpected runtime errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .error("Runtime Error")
                .message("A runtime error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Global exception handler for all uncaught exceptions.
     * Returns 500 Internal Server Error for unexpected errors.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request
    ) {
        logException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
                .stackTrace(includeStackTrace ? getStackTrace(ex) : null)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
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
                    method, path, status.value(), errorCode, correlationId, ex.getMessage(),
                    ex
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
