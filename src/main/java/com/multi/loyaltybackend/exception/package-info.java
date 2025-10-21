/**
 * Exception Handling for the Loyalty Backend Application.
 * <p>
 * This package contains custom exceptions and global exception handlers that provide
 * consistent error responses across the API.
 *
 * <h2>Exception Categories</h2>
 * <ul>
 *   <li><strong>Authentication:</strong> {@code InvalidPasswordResetTokenException}, {@code EmailAlreadyExistsException}</li>
 *   <li><strong>Resource Not Found:</strong> {@code UserNotFoundException}, {@code EventNotFoundException}, etc.</li>
 *   <li><strong>Business Rules:</strong> {@code InsufficientPointsException}, {@code VoucherExpiredException}</li>
 *   <li><strong>File Operations:</strong> {@code FileStorageException}, {@code InvalidFilePathException}</li>
 *   <li><strong>Conflicts:</strong> {@code VoucherAlreadyExchangedException}, {@code DuplicateRegistrationException}</li>
 * </ul>
 *
 * <h2>Exception Handling</h2>
 * <p>
 * All exceptions are caught by {@link com.multi.loyaltybackend.exception.GlobalExceptionHandler}
 * which returns standardized error responses:
 *
 * <pre>
 * {
 *   "timestamp": "2025-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Resource Not Found",
 *   "message": "User not found with id: 123",
 *   "path": "/api/users/123"
 * }
 * </pre>
 *
 * <h2>HTTP Status Mapping</h2>
 * <ul>
 *   <li><strong>400 Bad Request:</strong> Validation errors, business rule violations</li>
 *   <li><strong>401 Unauthorized:</strong> Authentication failures</li>
 *   <li><strong>404 Not Found:</strong> Resource not found exceptions</li>
 *   <li><strong>409 Conflict:</strong> Duplicate resources, state conflicts</li>
 *   <li><strong>500 Internal Server Error:</strong> Unexpected errors</li>
 * </ul>
 *
 * <h2>Recommended Refactoring</h2>
 * <p>
 * For better organization, exceptions should be grouped by domain:
 * <pre>
 * exception/
 * ├── handler/     - GlobalExceptionHandler, ErrorResponse
 * ├── auth/        - Authentication exceptions
 * ├── user/        - User-related exceptions
 * ├── event/       - Event-related exceptions
 * ├── company/     - Company exceptions
 * ├── voucher/     - Voucher exceptions
 * └── storage/     - File storage exceptions
 * </pre>
 *
 * @see com.multi.loyaltybackend.exception.GlobalExceptionHandler
 * @since 1.0.0
 */
package com.multi.loyaltybackend.exception;
