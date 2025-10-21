# Loyalty Backend - Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
│              (Web Browser, Mobile App, Third Party)             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS/REST
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      API Gateway Layer                          │
│                    (Spring Security + JWT)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ JWT Auth     │  │ OAuth2       │  │ CORS         │         │
│  │ Filter       │  │ Login        │  │ Config       │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                     Controller Layer                            │
│              (REST API Endpoints - JSON)                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  Auth    │ │  User    │ │  Event   │ │  Company │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────┐ ┌──────────┐                                     │
│  │ Voucher  │ │ Storage  │                                     │
│  └──────────┘ └──────────┘                                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      Service Layer                              │
│                  (Business Logic)                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Auth Service      User Service      Event Service      │   │
│  │  Email Service     Company Service   Voucher Service    │   │
│  │  Storage Service   JWT Service       Registration Svc   │   │
│  └─────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                   Repository Layer                              │
│              (Data Access - Spring Data JPA)                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  UserRepository    EventRepository    CompanyRepository  │  │
│  │  VoucherRepository RegistrationRepository                │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      Data Layer                                 │
│                (H2 Database - Development)                      │
│                (PostgreSQL/MySQL - Production)                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  users   │ │  events  │ │companies │ │ vouchers │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────────┐ ┌────────────────┐                           │
│  │registrations │ │ user_vouchers  │                           │
│  └──────────────┘ └────────────────┘                           │
└─────────────────────────────────────────────────────────────────┘

  ┌─────────────────────────────────────────────────────────────┐
  │               External Services                              │
  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
  │  │ SMTP Server  │  │ Google OAuth │  │ File System  │      │
  │  │ (Email)      │  │              │  │ (Images)     │      │
  │  └──────────────┘  └──────────────┘  └──────────────┘      │
  └─────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Controller Layer
**Responsibility:** Handle HTTP requests and responses

**Components:**
- AuthController - Authentication endpoints
- UserController (ProfileController) - User management
- EventController - Event management
- CompanyController - Company operations
- VoucherController - Voucher management
- StorageController - File upload/download

**Characteristics:**
- Validates request data (`@Valid`)
- Delegates to service layer
- Returns appropriate HTTP status codes
- Maps entities to DTOs

### 2. Service Layer
**Responsibility:** Implement business logic and orchestrate operations

**Components:**
- AuthService - User registration, login, password reset
- UserService - Profile management
- EventService - Event CRUD operations
- RegistrationService - Event registration, points awarding
- CompanyService - Company management
- VoucherService - Voucher exchange, validation
- StorageService - File operations
- JwtService - Token generation/validation
- EmailService - Email notifications

**Characteristics:**
- `@Transactional` for data consistency
- Business rule validation
- Throws domain-specific exceptions
- Stateless operations

### 3. Repository Layer
**Responsibility:** Data persistence and retrieval

**Components:**
- Extends JpaRepository
- Custom queries using `@Query`
- Specifications for dynamic queries

**Characteristics:**
- Direct database access
- Spring Data JPA auto-implementation
- Custom query methods when needed

### 4. Security Layer
**Responsibility:** Authentication and authorization

**Components:**
- SecurityConfig - Security rules
- JwtAuthFilter - Token validation
- CustomOAuth2UserService - OAuth2 handling
- JwtService - Token operations

**Flow:**
```
Request → JwtAuthFilter → Extract Token → Validate → SecurityContext → Controller
```

## Module Architecture

### Well-Organized Modules (Current)

#### Company Module
```
company/
├── controller/CompanyController.java
├── service/CompanyService.java
├── repository/CompanyRepository.java
└── model/Company.java
```

#### Voucher Module
```
voucher/
├── controller/VoucherController.java
├── service/VoucherService.java
├── repository/
│   ├── VoucherRepository.java
│   └── UserVoucherRepository.java
├── model/
│   ├── Voucher.java
│   └── UserVoucher.java
└── dto/
    ├── VoucherRequest.java
    └── UserVoucherRequest.java
```

**These modules demonstrate the target structure for all features.**

## Data Flow Examples

### User Registration Flow
```
1. POST /api/auth/register
   ↓
2. AuthController.register()
   ↓
3. AuthService.register()
   ├─→ Check email exists (UserRepository)
   ├─→ Encode password (PasswordEncoder)
   ├─→ Create User entity
   └─→ Save to database (UserRepository)
   ↓
4. Return success response
```

### Event Registration with Points
```
1. POST /api/events/register
   ↓
2. EventController (delegates)
   ↓
3. RegistrationService.registerUserToEvent()
   ├─→ Validate user exists (UserRepository)
   ├─→ Validate event exists (EventRepository)
   ├─→ Check for duplicate (RegistrationRepository)
   ├─→ Create registration
   ├─→ Award points (user.incrementPoints())
   ├─→ Increment event count
   └─→ Save all changes (@Transactional)
   ↓
4. Return registration response
```

### Voucher Exchange Flow
```
1. POST /api/vouchers/exchange
   ↓
2. VoucherController (delegates)
   ↓
3. VoucherService.exchangeVoucher()
   ├─→ Validate user exists
   ├─→ Validate voucher exists
   ├─→ Check voucher expiry ❌ Throw if expired
   ├─→ Check duplicate exchange ❌ Throw if already exchanged
   ├─→ Check points sufficiency ❌ Throw if insufficient
   ├─→ Deduct points from user
   ├─→ Create UserVoucher association
   └─→ Save all changes (@Transactional)
   ↓
4. Return voucher details
```

## Exception Handling Flow

```
Controller/Service → Throws Exception
         ↓
GlobalExceptionHandler catches
         ↓
Maps to appropriate HTTP status
         ↓
Creates ErrorResponse
         ↓
Returns JSON error response
```

**Exception Mapping:**
- `ResourceNotFoundException` → 404 Not Found
- `InsufficientPointsException` → 400 Bad Request
- `VoucherExpiredException` → 400 Bad Request
- `EmailAlreadyExistsException` → 409 Conflict
- `InvalidPasswordResetTokenException` → 400 Bad Request
- `FileStorageException` → 500 Internal Server Error

## Security Architecture

### Authentication Mechanisms

1. **JWT Authentication**
```
Login → Generate JWT Token → Client stores token
Request → Include token in header → Validate → Grant access
```

2. **OAuth2 (Google)**
```
Click Google Login → Redirect to Google → User authorizes
→ Google redirects back → Create/update user → Generate JWT → Return to client
```

3. **Token Blacklist**
```
Logout → Add token to blacklist (ConcurrentHashMap)
Future requests → Check blacklist → Reject if found
```

### Authorization Levels

- **Public:** Register, login, password reset, images, docs
- **Authenticated:** All other endpoints
- **Method-Level:** Can be added with `@PreAuthorize`

## Database Schema

### Core Tables

**users**
- id (PK)
- email (unique)
- password (hashed)
- full_name
- total_points
- event_count
- role
- password_reset_token
- password_reset_token_expiry
- created_at, updated_at

**events**
- id (PK)
- title
- description
- date_time
- location
- points (awarded on registration)
- category
- created_at, updated_at

**companies**
- id (PK)
- name
- logo_file_name
- created_at, updated_at

**vouchers**
- id (PK)
- title
- description
- points (cost)
- expiry
- company_id (FK)

**registrations**
- id (PK)
- user_id (FK)
- event_id (FK)
- comment
- status
- registered_at, updated_at

**user_vouchers**
- id (PK)
- user_id (FK)
- voucher_id (FK)
- status
- exchanged_at

## Configuration Management

### Environment-Based Configuration

```
Development:
  - H2 database (in-memory)
  - SQL logging enabled
  - H2 console enabled

Production:
  - PostgreSQL/MySQL
  - SQL logging disabled
  - H2 console disabled
  - All secrets from environment variables
```

### Configuration Files

- `application.properties` - Main config with env var placeholders
- `.env` - Local environment variables (gitignored)
- `.env.example` - Template for required variables

## Scalability Considerations

### Horizontal Scaling
- Stateless services (can run multiple instances)
- Externalize session (JWT is stateless)
- Shared database (can later shard)

### Performance Optimizations
- Lazy loading for associations
- Pagination for list endpoints
- Database indexes on foreign keys
- Connection pooling (HikariCP)

### Future Microservices
Current modular structure allows easy extraction:
- Auth Service
- User Service
- Event Service
- Voucher Service
- Company Service
- Storage Service

Each module has clear boundaries and minimal cross-dependencies.

## Monitoring & Observability

### Actuator Endpoints
- `/actuator/health` - Health check
- Other endpoints available (configure as needed)

### Logging
- Service layer logs business operations
- Registration service logs point awarding
- Exception handler logs errors

### API Documentation
- Swagger UI at `/swagger-ui.html`
- OpenAPI spec at `/v3/api-docs`

## Best Practices Applied

✅ Layered architecture (Controller → Service → Repository)
✅ Dependency injection (Spring's @Autowired)
✅ Transaction management (@Transactional)
✅ Exception handling (GlobalExceptionHandler)
✅ DTO pattern (separate from entities)
✅ Repository pattern (Spring Data JPA)
✅ Configuration externalization (environment variables)
✅ Security by default (JWT + OAuth2)
✅ API documentation (Swagger/OpenAPI)
✅ Consistent error responses (ErrorResponse)
✅ Pagination support (Pageable)
✅ Lazy loading (performance)

## Recommended Next Steps

1. **Testing**
   - Unit tests for services
   - Integration tests for controllers
   - Test coverage > 80%

2. **Refactoring**
   - Organize into feature modules (see STRUCTURE_IMPROVEMENT_GUIDE.md)
   - Extract security to separate package
   - Group exceptions by domain

3. **Production Readiness**
   - Migrate to production database
   - Implement Redis for token blacklist
   - Add rate limiting
   - Set up monitoring/alerting
   - Configure logging (ELK stack)

4. **Features**
   - Email verification
   - Admin dashboard
   - Notification system
   - Analytics/reporting
