package com.multi.loyaltybackend.exception;

/**
 * Enumeration of all error codes used in the application.
 * Each error code has a unique identifier and HTTP status code mapping.
 *
 * Error Code Format: CATEGORY_SPECIFIC_ERROR
 * Categories:
 * - RESOURCE_* : Resource not found errors (404)
 * - AUTH_* : Authentication/Authorization errors (401, 403)
 * - VALIDATION_* : Validation errors (400)
 * - BUSINESS_* : Business logic errors (400, 409)
 * - SYSTEM_* : System/Server errors (500)
 */
public enum ErrorCode {

    // Resource Not Found Errors (404)
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "The requested resource was not found"),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    COMPANY_NOT_FOUND("COMPANY_NOT_FOUND", "Company not found"),
    EVENT_NOT_FOUND("EVENT_NOT_FOUND", "Event not found"),
    VOUCHER_NOT_FOUND("VOUCHER_NOT_FOUND", "Voucher not found"),

    // Authentication & Authorization Errors (401, 403)
    AUTHENTICATION_FAILED("AUTH_FAILED", "Authentication failed"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid username or password"),
    INVALID_TOKEN("INVALID_TOKEN", "Invalid or expired token"),
    INVALID_PASSWORD_RESET_TOKEN("INVALID_PASSWORD_RESET_TOKEN", "Invalid password reset token"),
    PASSWORD_RESET_TOKEN_EXPIRED("PASSWORD_RESET_TOKEN_EXPIRED", "Password reset token has expired"),
    INVALID_PASSWORD_RESET_CODE("INVALID_PASSWORD_RESET_CODE", "Invalid password reset code"),
    PASSWORD_RESET_CODE_EXPIRED("PASSWORD_RESET_CODE_EXPIRED", "Password reset code has expired"),
    INVALID_EMAIL_VERIFICATION_TOKEN("INVALID_EMAIL_VERIFICATION_TOKEN", "Invalid email verification token"),
    EMAIL_VERIFICATION_TOKEN_EXPIRED("EMAIL_VERIFICATION_TOKEN_EXPIRED", "Email verification token has expired"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "Unauthorized access to resource"),

    // Validation Errors (400)
    VALIDATION_FAILED("VALIDATION_FAILED", "Input validation failed"),
    INVALID_INPUT("INVALID_INPUT", "Invalid input provided"),
    INVALID_FILE_PATH("INVALID_FILE_PATH", "Invalid file path"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Invalid file type"),

    // Business Logic Errors (400, 409)
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email address already registered"),
    DUPLICATE_REGISTRATION("DUPLICATE_REGISTRATION", "Already registered for this event"),
    VOUCHER_ALREADY_EXCHANGED("VOUCHER_ALREADY_EXCHANGED", "Voucher already exchanged by user"),
    INSUFFICIENT_POINTS("INSUFFICIENT_POINTS", "Insufficient points for this operation"),
    VOUCHER_EXPIRED("VOUCHER_EXPIRED", "Voucher has expired"),
    INVALID_OPERATION("INVALID_OPERATION", "Invalid operation"),

    // File Storage Errors (500)
    FILE_STORAGE_ERROR("FILE_STORAGE_ERROR", "Error occurred during file storage operation"),
    FILE_DELETION_ERROR("FILE_DELETION_ERROR", "Error occurred during file deletion"),
    FILE_RETRIEVAL_ERROR("FILE_RETRIEVAL_ERROR", "Error occurred during file retrieval"),

    // Database Errors (500)
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed"),
    DATA_INTEGRITY_ERROR("DATA_INTEGRITY_ERROR", "Data integrity constraint violated"),

    // System Errors (500)
    INTERNAL_SERVER_ERROR("INTERNAL_ERROR", "An internal server error occurred"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service temporarily unavailable"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service call failed"),

    // Generic Errors
    UNKNOWN_ERROR("UNKNOWN_ERROR", "An unknown error occurred");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}
