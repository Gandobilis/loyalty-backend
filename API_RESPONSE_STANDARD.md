# API Response Standard

This document describes the standardized response format used across all API endpoints in the loyalty-backend application.

## Overview

All API responses follow a consistent structure using the `ApiResponse<T>` wrapper class. This ensures:

- **Consistent Format**: All responses (success and error) have the same structure
- **Easy Error Handling**: Clients can check `success` field to determine outcome
- **Request Tracking**: Every response includes a correlation ID
- **Rich Context**: Additional metadata for debugging and monitoring
- **Type Safety**: Generic type support for different data types

## Response Structure

### Success Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "path": "/api/users/1"
}
```

### Error Response

```json
{
  "success": false,
  "error": {
    "errorCode": "USER_NOT_FOUND",
    "error": "Not Found",
    "message": "User not found with id: 999",
    "context": {
      "userId": 999,
      "resourceName": "User",
      "fieldName": "id",
      "fieldValue": 999
    }
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "path": "/api/users/999"
}
```

## Response Fields

| Field | Type | Present | Description |
|-------|------|---------|-------------|
| `success` | boolean | Always | Indicates if the request was successful |
| `message` | string | Optional | Success message or additional information |
| `data` | T (generic) | Success only | The response payload |
| `error` | ErrorDetails | Error only | Detailed error information |
| `timestamp` | LocalDateTime | Always | When the response was generated |
| `correlationId` | string | Always | Unique ID for request tracking |
| `path` | string | Always | The request path |

### ErrorDetails Structure

| Field | Type | Present | Description |
|-------|------|---------|-------------|
| `errorCode` | string | Always | Programmatic error identifier (e.g., "USER_NOT_FOUND") |
| `error` | string | Always | Human-readable error type (e.g., "Not Found") |
| `message` | string | Always | Detailed error message |
| `context` | object | Optional | Additional context data for debugging |
| `stackTrace` | string | Optional | Stack trace (only when debug mode enabled) |

## Usage in Controllers

### Using ResponseUtil (Recommended)

The `ResponseUtil` class provides convenient methods for creating standardized responses:

```java
import com.multi.loyaltybackend.dto.ApiResponse;
import com.multi.loyaltybackend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get user by ID
     * Returns: 200 OK with user data, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id); // throws UserNotFoundException if not found
        return ResponseUtil.success(user);
    }

    /**
     * Get all users
     * Returns: 200 OK with list of users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseUtil.success(users, "Successfully retrieved " + users.size() + " users");
    }

    /**
     * Create new user
     * Returns: 201 Created with new user data
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserRequest request) {
        User newUser = userService.create(request);
        return ResponseUtil.created(newUser, "User created successfully");
    }

    /**
     * Update user
     * Returns: 200 OK with updated user data
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        User updatedUser = userService.update(id, request);
        return ResponseUtil.success(updatedUser, "User updated successfully");
    }

    /**
     * Delete user
     * Returns: 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseUtil.noContent();
    }

    /**
     * Deactivate user (soft delete)
     * Returns: 200 OK with confirmation message
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseUtil.success(null, "User deactivated successfully");
    }
}
```

### Using ApiResponse Builder

For more control, use the builder pattern directly:

```java
@GetMapping("/{id}/summary")
public ResponseEntity<ApiResponse<UserSummary>> getUserSummary(@PathVariable Long id) {
    UserSummary summary = userService.getSummary(id);

    ApiResponse<UserSummary> response = ApiResponse.<UserSummary>builder()
            .success(true)
            .message("User summary retrieved successfully")
            .data(summary)
            .timestamp(LocalDateTime.now())
            .correlationId(CorrelationIdFilter.getCurrentCorrelationId())
            .path("/api/users/" + id + "/summary")
            .build();

    return ResponseEntity.ok(response);
}
```

## Common Response Patterns

### 1. Simple Success (200 OK)

```java
@GetMapping("/health")
public ResponseEntity<ApiResponse<String>> health() {
    return ResponseUtil.success("Service is running");
}
```

**Response:**
```json
{
  "success": true,
  "data": "Service is running",
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/health"
}
```

### 2. Success with Message (200 OK)

```java
@PostMapping("/{id}/activate")
public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable Long id) {
    User user = userService.activate(id);
    return ResponseUtil.success(user, "User account activated successfully");
}
```

**Response:**
```json
{
  "success": true,
  "message": "User account activated successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "status": "ACTIVE"
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/users/1/activate"
}
```

### 3. Created (201 Created)

```java
@PostMapping
public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserRequest request) {
    User newUser = userService.create(request);
    return ResponseUtil.created(newUser);
}
```

**Response:**
```json
{
  "success": true,
  "message": "Resource created successfully",
  "data": {
    "id": 5,
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/users"
}
```

### 4. No Content (204 No Content)

```java
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseUtil.noContent();
}
```

**Response:** Empty body with 204 status code

### 5. Accepted (202 Accepted)

```java
@PostMapping("/batch-import")
public ResponseEntity<ApiResponse<Void>> importUsers(@RequestBody List<UserRequest> users) {
    batchService.scheduleImport(users);
    return ResponseUtil.accepted("Batch import scheduled for processing");
}
```

**Response:**
```json
{
  "success": true,
  "message": "Batch import scheduled for processing",
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/users/batch-import"
}
```

## Error Responses

Exceptions are automatically handled by `GlobalExceptionHandler` and converted to the standard format:

### 1. Resource Not Found (404)

```java
// Just throw the exception - it's automatically handled
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
    User user = userService.findById(id); // throws UserNotFoundException
    return ResponseUtil.success(user);
}
```

**Response when user not found:**
```json
{
  "success": false,
  "error": {
    "errorCode": "USER_NOT_FOUND",
    "error": "Not Found",
    "message": "User not found with id: 999",
    "context": {
      "userId": 999
    }
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/users/999"
}
```

### 2. Validation Error (400)

```java
@PostMapping
public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserRequest request) {
    User newUser = userService.create(request);
    return ResponseUtil.created(newUser);
}
```

**Response when validation fails:**
```json
{
  "success": false,
  "error": {
    "errorCode": "VALIDATION_FAILED",
    "error": "Validation Failed",
    "message": "email: Email is required, name: Name must be at least 2 characters",
    "context": {
      "fieldErrors": {
        "email": "Email is required",
        "name": "Name must be at least 2 characters"
      }
    }
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/users"
}
```

### 3. Business Logic Error (400)

```java
@PostMapping("/{userId}/exchange-voucher")
public ResponseEntity<ApiResponse<UserVoucher>> exchangeVoucher(
        @PathVariable Long userId,
        @RequestBody VoucherRequest request
) {
    UserVoucher exchange = voucherService.exchange(userId, request.getVoucherId());
    return ResponseUtil.created(exchange, "Voucher exchanged successfully");
}
```

**Response when insufficient points:**
```json
{
  "success": false,
  "error": {
    "errorCode": "INSUFFICIENT_POINTS",
    "error": "Insufficient Points",
    "message": "Insufficient points. Required: 500, Available: 250",
    "context": {
      "requiredPoints": 500,
      "availablePoints": 250
    }
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/users/1/exchange-voucher"
}
```

### 4. Conflict (409)

```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
    User newUser = authService.register(request);
    return ResponseUtil.created(newUser, "User registered successfully");
}
```

**Response when email already exists:**
```json
{
  "success": false,
  "error": {
    "errorCode": "EMAIL_ALREADY_EXISTS",
    "error": "Resource Conflict",
    "message": "User with email already exists: john@example.com",
    "context": {
      "email": "john@example.com"
    }
  },
  "timestamp": "2025-10-21T10:30:00",
  "correlationId": "...",
  "path": "/api/auth/register"
}
```

## Client-Side Usage

### JavaScript/TypeScript Example

```typescript
interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: ErrorDetails;
  timestamp: string;
  correlationId: string;
  path: string;
}

interface ErrorDetails {
  errorCode: string;
  error: string;
  message: string;
  context?: any;
  stackTrace?: string;
}

// Fetch user
async function getUser(userId: number): Promise<User> {
  const response = await fetch(`/api/users/${userId}`);
  const apiResponse: ApiResponse<User> = await response.json();

  if (!apiResponse.success) {
    // Handle error based on error code
    switch (apiResponse.error?.errorCode) {
      case 'USER_NOT_FOUND':
        throw new Error('User not found');
      case 'UNAUTHORIZED':
        // Redirect to login
        window.location.href = '/login';
        break;
      default:
        throw new Error(apiResponse.error?.message || 'Unknown error');
    }
  }

  return apiResponse.data!;
}

// Create user
async function createUser(userData: UserRequest): Promise<User> {
  const response = await fetch('/api/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  });

  const apiResponse: ApiResponse<User> = await response.json();

  if (!apiResponse.success) {
    if (apiResponse.error?.errorCode === 'VALIDATION_FAILED') {
      // Display field-specific errors
      const fieldErrors = apiResponse.error.context?.fieldErrors;
      console.error('Validation errors:', fieldErrors);
    }
    throw new Error(apiResponse.error?.message || 'Failed to create user');
  }

  return apiResponse.data!;
}

// Generic API call handler
async function apiCall<T>(
  url: string,
  options?: RequestInit
): Promise<T> {
  const response = await fetch(url, options);
  const apiResponse: ApiResponse<T> = await response.json();

  if (!apiResponse.success) {
    console.error(
      `API Error [${apiResponse.error?.errorCode}]:`,
      apiResponse.error?.message,
      `\nCorrelation ID: ${apiResponse.correlationId}`
    );
    throw new Error(apiResponse.error?.message || 'API call failed');
  }

  return apiResponse.data!;
}
```

### React Example

```tsx
import { useState, useEffect } from 'react';

function UserProfile({ userId }: { userId: number }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`/api/users/${userId}`)
      .then(res => res.json())
      .then((apiResponse: ApiResponse<User>) => {
        if (apiResponse.success) {
          setUser(apiResponse.data);
        } else {
          setError(apiResponse.error?.message || 'Failed to load user');
        }
      })
      .catch(err => setError('Network error'))
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!user) return <div>User not found</div>;

  return (
    <div>
      <h2>{user.name}</h2>
      <p>{user.email}</p>
    </div>
  );
}
```

## Best Practices

### 1. Always Use ResponseUtil

```java
// ✅ Good - uses ResponseUtil
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseUtil.success(user);
}

// ❌ Avoid - direct ResponseEntity
@GetMapping("/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok(user);
}
```

### 2. Let Exceptions Be Handled Automatically

```java
// ✅ Good - throw exception, let handler deal with it
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userService.findById(id); // throws UserNotFoundException
    return ResponseUtil.success(user);
}

// ❌ Avoid - manual error handling
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    try {
        User user = userService.findById(id);
        return ResponseUtil.success(user);
    } catch (UserNotFoundException ex) {
        return ResponseUtil.notFound(ex.getMessage());
    }
}
```

### 3. Provide Meaningful Messages

```java
// ✅ Good - clear, actionable message
return ResponseUtil.created(newUser, "User account created successfully");

// ❌ Avoid - generic or missing message
return ResponseUtil.created(newUser);
```

### 4. Use Correlation IDs for Support

When users report errors, ask for the correlation ID:

```java
// The correlation ID is automatically included in every response
// Users can provide it for support:
// "I got an error with correlation ID: a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6"

// Then search logs:
grep "a1b2c3d4-e5f6-7g8h" application.log
```

## Migration Guide

### Before (Direct Entity Response)

```java
@GetMapping("/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    return ResponseEntity.ok(user);
}
```

### After (Standard Response)

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
    User user = userService.findById(id); // service throws exception
    return ResponseUtil.success(user);
}
```

## Benefits

1. **Consistency**: All responses follow the same structure
2. **Error Handling**: Clients can easily detect and handle errors
3. **Debugging**: Correlation IDs enable request tracking
4. **Type Safety**: Generic support for different data types
5. **Monitoring**: Standard format enables easy metrics collection
6. **Documentation**: Clear, predictable API behavior

## See Also

- [EXCEPTION_HANDLING.md](EXCEPTION_HANDLING.md) - Exception handling details
- [ErrorCode.java](src/main/java/com/multi/loyaltybackend/exception/ErrorCode.java) - All error codes
- [ResponseUtil.java](src/main/java/com/multi/loyaltybackend/util/ResponseUtil.java) - Response utility methods
