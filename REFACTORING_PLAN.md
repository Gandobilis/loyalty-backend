# Project Structure Refactoring Plan

## Current Issues
1. Mixed structure - some modules (company, voucher) are well organized, others are scattered
2. Root-level services, controllers, models mixed together
3. Security and config classes mixed
4. No clear module boundaries

## Target Structure

```
src/main/java/com/multi/loyaltybackend/
├── auth/                           # Authentication Module
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   └── EmailService.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── RegisterRequest.java
│       └── PasswordResetRequest.java
│
├── user/                           # User & Profile Module
│   ├── controller/
│   │   └── UserController.java (renamed from ProfileController)
│   ├── service/
│   │   └── UserService.java (renamed from ProfileService)
│   ├── repository/
│   │   └── UserRepository.java
│   ├── model/
│   │   ├── User.java
│   │   └── Role.java (enum)
│   └── dto/
│       ├── UserProfileResponse.java
│       └── UserUpdateRequest.java
│
├── event/                          # Event & Registration Module
│   ├── controller/
│   │   └── EventController.java
│   ├── service/
│   │   ├── EventService.java
│   │   └── RegistrationService.java
│   ├── repository/
│   │   ├── EventRepository.java
│   │   └── RegistrationRepository.java
│   ├── model/
│   │   ├── Event.java
│   │   ├── Registration.java
│   │   ├── EventCategory.java (enum)
│   │   └── RegistrationStatus.java (enum)
│   └── dto/
│       ├── EventDTO.java
│       ├── EventRequest.java
│       ├── RegistrationResponse.java
│       └── EventSpecifications.java
│
├── company/                        # Company Module (already well structured)
│   ├── controller/
│   │   └── CompanyController.java
│   ├── service/
│   │   └── CompanyService.java
│   ├── repository/
│   │   └── CompanyRepository.java
│   └── model/
│       └── Company.java
│
├── voucher/                        # Voucher Module (already well structured)
│   ├── controller/
│   │   └── VoucherController.java
│   ├── service/
│   │   └── VoucherService.java
│   ├── repository/
│   │   ├── VoucherRepository.java
│   │   └── UserVoucherRepository.java
│   ├── model/
│   │   ├── Voucher.java
│   │   ├── UserVoucher.java
│   │   └── VoucherStatus.java (enum)
│   └── dto/
│       ├── VoucherRequest.java
│       └── UserVoucherRequest.java
│
├── storage/                        # File Storage Module
│   ├── controller/
│   │   └── StorageController.java (renamed from ImageStorageController)
│   └── service/
│       └── StorageService.java (renamed from ImageStorageService)
│
├── security/                       # Security Components
│   ├── jwt/
│   │   ├── JwtAuthFilter.java
│   │   └── JwtService.java (move from auth)
│   └── oauth2/
│       └── CustomOAuth2UserService.java
│
├── config/                         # Configuration Classes
│   ├── SecurityConfig.java
│   ├── OpenAPIConfig.java
│   └── JpaAuditingConfig.java (if needed)
│
├── exception/                      # Exception Handling
│   ├── handler/
│   │   ├── GlobalExceptionHandler.java
│   │   └── ErrorResponse.java
│   ├── auth/
│   │   ├── InvalidPasswordResetTokenException.java
│   │   ├── PasswordResetTokenExpiredException.java
│   │   └── EmailAlreadyExistsException.java
│   ├── user/
│   │   └── UserNotFoundException.java
│   ├── event/
│   │   ├── EventNotFoundException.java
│   │   └── DuplicateRegistrationException.java
│   ├── company/
│   │   └── CompanyNotFoundException.java
│   ├── voucher/
│   │   ├── VoucherNotFoundException.java
│   │   ├── VoucherExpiredException.java
│   │   ├── VoucherAlreadyExchangedException.java
│   │   └── InsufficientPointsException.java
│   ├── storage/
│   │   ├── FileStorageException.java
│   │   └── InvalidFilePathException.java
│   └── common/
│       └── ResourceNotFoundException.java
│
└── LoyaltyBackendApplication.java

```

## Benefits

1. **Clear Module Boundaries**: Each feature/domain has its own package
2. **Consistent Structure**: Every module follows the same pattern (controller, service, repository, model, dto)
3. **Easy Navigation**: Developers can quickly find related code
4. **Scalability**: Easy to add new modules
5. **Separation of Concerns**: Security, config, and exceptions are separated
6. **Testability**: Modular structure makes unit testing easier

## Migration Steps

1. Create new package structure
2. Move authentication related classes to auth module
3. Move user/profile classes to user module
4. Move event/registration classes to event module
5. Move storage classes to storage module
6. Reorganize security components
7. Reorganize exception classes by domain
8. Update all import statements
9. Test compilation and functionality
10. Update documentation

## Naming Improvements

- `ProfileController` → `UserController`
- `ProfileService` → `UserService`
- `ImageStorageController` → `StorageController`
- `ImageStorageService` → `StorageService`
- More descriptive DTO names (LoginRequest, RegisterRequest, etc.)
