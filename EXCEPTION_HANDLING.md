# Enterprise-Level Exception Handling

This document describes the enhanced exception handling system implemented in the loyalty-backend application.

## Overview

The application uses a comprehensive, enterprise-level exception handling system with the following features:

- **Error Codes**: Unique, programmatic identifiers for each error type
- **Correlation IDs**: Request tracking across the entire system
- **Comprehensive Logging**: Full context logging with structured error information
- **Consistent Error Responses**: Standardized error format across all endpoints
- **Environment-Aware Debug Info**: Stack traces and context in development mode
- **Centralized Exception Management**: Single point of exception handling

## Architecture

### 1. Error Codes (`ErrorCode.java`)

All errors are identified by unique error codes defined in the `ErrorCode` enum:

```java
public enum ErrorCode {
    RESOURCE_NOT_FOUND,
    USER_NOT_FOUND,
    COMPANY_NOT_FOUND,
    AUTHENTICATION_FAILED,
    VALIDATION_FAILED,
    INSUFFICIENT_POINTS,
    // ... and more
}
```

**Categories:**
- `RESOURCE_*`: Resource not found errors (404)
- `AUTH_*`: Authentication/Authorization errors (401, 403)
- `VALIDATION_*`: Validation errors (400)
- `BUSINESS_*`: Business logic errors (400, 409)
- `SYSTEM_*`: System/Server errors (500)

### 2. Base Exception Class (`BaseException.java`)

All custom exceptions extend `BaseException`, which provides:

```java
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> context;

    // Methods for adding contextual information
    public BaseException addContext(String key, Object value);
}
```

**Benefits:**
- Consistent error structure
- Automatic error code assignment
- HTTP status mapping
- Contextual information for debugging

### 3. Enhanced Error Response (`ErrorResponse.java`)

The error response includes comprehensive information:

```json
{
  "timestamp": "2025-10-21T10:30:00",
  "status": 404,
  "errorCode": "USER_NOT_FOUND",
  "error": "Not Found",
  "message": "User not found with id: 123",
  "path": "/api/users/123",
  "correlationId": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "context": {
    "userId": 123
  },
  "stackTrace": null
}
```

**Fields:**
- `timestamp`: When the error occurred
- `status`: HTTP status code
- `errorCode`: Programmatic error identifier
- `error`: Human-readable error type
- `message`: Detailed error message
- `path`: Request path that caused the error
- `correlationId`: Unique ID for tracking this request
- `context`: Additional context data (only in debug mode or with specific data)
- `stackTrace`: Stack trace (only when `app.debug.include-stack-trace=true`)

### 4. Correlation ID Filter (`CorrelationIdFilter.java`)

Automatically generates and tracks correlation IDs for each request:

- Accepts client-provided correlation IDs via `X-Correlation-ID` header
- Generates UUID if not provided
- Adds to MDC (Mapped Diagnostic Context) for logging
- Returns in response header for client tracking

**Usage in logs:**
```
2025-10-21 10:30:00.123 [http-nio-8080-exec-1] ERROR c.m.l.service.UserService [CorrelationId=a1b2c3d4-...] - User not found
```

### 5. Global Exception Handler (`GlobalExceptionHandler.java`)

Centralized exception handling with:

- **Comprehensive logging**: All exceptions logged with full context
- **Error level mapping**:
  - 5xx errors → `log.error()` with full stack trace
  - 4xx errors → `log.warn()` with message
  - Other errors → `log.info()`
- **Environment-aware responses**: Include stack traces based on configuration
- **Specific handlers** for each exception type

## Exception Hierarchy

```
RuntimeException
└── BaseException (abstract)
    ├── ResourceNotFoundException
    │   ├── UserNotFoundException
    │   ├── CompanyNotFoundException
    │   ├── EventNotFoundException
    │   └── VoucherNotFoundException
    ├── EmailAlreadyExistsException
    ├── DuplicateRegistrationException
    ├── VoucherAlreadyExchangedException
    ├── InsufficientPointsException
    ├── VoucherExpiredException
    ├── InvalidPasswordResetTokenException
    ├── PasswordResetTokenExpiredException
    ├── FileStorageException
    └── InvalidFilePathException
```

## Configuration

### Application Properties

```properties
# Enable stack traces in error responses (development only)
app.debug.include-stack-trace=false

# Logging levels
logging.level.com.multi.loyaltybackend=INFO

# Logging pattern with correlation ID
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [CorrelationId=%X{correlationId}] - %msg%n
```

### Environment-Specific Settings

**Development:**
```properties
app.debug.include-stack-trace=true
logging.level.com.multi.loyaltybackend=DEBUG
```

**Production:**
```properties
app.debug.include-stack-trace=false
logging.level.com.multi.loyaltybackend=INFO
```

## Usage Examples

### 1. Throwing Custom Exceptions

```java
@Service
public class UserService {

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void exchangeVoucher(Long userId, Long voucherId) {
        User user = findById(userId);
        Voucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new VoucherNotFoundException(voucherId));

        if (user.getPoints() < voucher.getPoints()) {
            throw new InsufficientPointsException(
                voucher.getPoints(),
                user.getPoints()
            );
        }

        // Exchange voucher...
    }
}
```

### 2. Adding Context to Exceptions

```java
throw new ResourceNotFoundException("Order", "id", orderId)
    .addContext("customerId", customerId)
    .addContext("attemptTime", LocalDateTime.now());
```

### 3. Client Error Handling

Clients can:
- Use error codes for programmatic handling
- Display user-friendly messages
- Track issues using correlation IDs
- Retry requests with the same correlation ID for idempotency

**Example client code:**
```javascript
try {
    const response = await fetch('/api/users/123');
    if (!response.ok) {
        const error = await response.json();

        // Use error code for programmatic handling
        if (error.errorCode === 'USER_NOT_FOUND') {
            showNotFoundMessage();
        } else if (error.errorCode === 'INSUFFICIENT_POINTS') {
            showInsufficientPointsDialog(error.context);
        }

        // Log correlation ID for support
        console.error('Error occurred:', error.correlationId);
    }
} catch (e) {
    // Handle network errors
}
```

## Monitoring and Debugging

### 1. Log Analysis

Search logs by correlation ID to trace the full request lifecycle:

```bash
grep "CorrelationId=a1b2c3d4-e5f6-7g8h" application.log
```

### 2. Error Metrics

Monitor error rates by error code:
- Track business logic errors vs system errors
- Alert on unusual error patterns
- Measure error resolution time

### 3. Debugging in Production

For production issues:
1. Get correlation ID from user or error response
2. Search logs using correlation ID
3. Review full request context and error details
4. If needed, temporarily enable stack traces for specific requests

## Best Practices

### 1. Exception Throwing

- **Use specific exceptions**: Prefer `UserNotFoundException` over generic `ResourceNotFoundException`
- **Include context**: Add relevant IDs and data to exceptions
- **Meaningful messages**: Write clear, actionable error messages

### 2. Exception Handling

- **Don't catch and ignore**: Log and rethrow or handle appropriately
- **Clean up resources**: Use try-with-resources or finally blocks
- **Transaction boundaries**: Let exceptions roll back transactions

### 3. Error Messages

- **Security**: Don't expose sensitive data in error messages
- **User-friendly**: Write messages that users can understand
- **Actionable**: Include what went wrong and how to fix it

### 4. Logging

- **Use structured logging**: Include correlation IDs and context
- **Appropriate log levels**: ERROR for 5xx, WARN for 4xx
- **Don't log sensitive data**: Passwords, tokens, PII

## Migration Guide

For existing exception handling code:

### Before:
```java
throw new RuntimeException("User not found with id: " + id);
```

### After:
```java
throw new UserNotFoundException(id);
```

### Before:
```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleException(UserNotFoundException ex) {
    return new ResponseEntity<>(
        new ErrorResponse(LocalDateTime.now(), 404, "Not Found", ex.getMessage(), "/api/users"),
        HttpStatus.NOT_FOUND
    );
}
```

### After:
No handler needed - `GlobalExceptionHandler` handles all `BaseException` subclasses automatically.

## Testing Exception Handling

### Unit Tests

```java
@Test
void shouldThrowUserNotFoundException() {
    assertThrows(UserNotFoundException.class, () -> {
        userService.findById(999L);
    });
}

@Test
void shouldIncludeErrorContext() {
    try {
        userService.exchangeVoucher(1L, 99L);
        fail("Expected InsufficientPointsException");
    } catch (InsufficientPointsException ex) {
        assertEquals(100, ex.getRequiredPoints());
        assertEquals(50, ex.getAvailablePoints());
        assertEquals(ErrorCode.INSUFFICIENT_POINTS, ex.getErrorCode());
    }
}
```

### Integration Tests

```java
@Test
void shouldReturnErrorResponseWithCorrelationId() {
    ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
        "/api/users/999",
        ErrorResponse.class
    );

    assertEquals(404, response.getStatusCode().value());
    assertEquals("USER_NOT_FOUND", response.getBody().getErrorCode());
    assertNotNull(response.getBody().getCorrelationId());
}
```

## Future Enhancements

Potential improvements for future releases:

1. **Retry Mechanisms**: Automatic retry for transient failures
2. **Circuit Breakers**: Prevent cascading failures
3. **Error Aggregation**: Group similar errors for analysis
4. **Custom Error Pages**: Branded error pages for web UI
5. **Internationalization**: Multi-language error messages
6. **Error Documentation**: Auto-generate API error documentation

## Support

For questions or issues with exception handling:
1. Check this documentation
2. Review logs with correlation ID
3. Contact the development team with correlation ID and error code
