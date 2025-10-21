# Project Structure Improvement Guide

## Current Structure Analysis

### ✅ Well-Organized Modules
- `company/` - Complete module with controller, service, repository, model
- `voucher/` - Complete module with controller, service, repository, model, dto

### ⚠️ Needs Organization
- Root `controller/`, `service/`, `repository/`, `model/`, `dto/` - Mixed concerns
- `config/` - Contains both general config and security
- `exception/` - All exceptions in one flat package

## Recommended Structure (Best Practice)

```
src/main/java/com/multi/loyaltybackend/
│
├── LoyaltyBackendApplication.java
│
├── auth/                                   # Authentication Module
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── EmailService.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── PasswordResetRequest.java
│
├── user/                                   # User/Profile Module
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   └── UserService.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── model/
│   │   ├── User.java
│   │   └── Role.java
│   └── dto/
│       └── UserProfileDTO.java
│
├── event/                                  # Event & Registration Module
│   ├── controller/
│   │   └── EventController.java
│   ├── service/
│   │   ├── EventService.java
│   │   └── RegistrationService.java
│   ├── repository/
│   │   ├── EventRepository.java
│   │   ├── RegistrationRepository.java
│   │   └── EventSpecifications.java
│   ├── model/
│   │   ├── Event.java
│   │   ├── Registration.java
│   │   ├── EventCategory.java
│   │   └── RegistrationStatus.java
│   └── dto/
│       ├── EventDTO.java
│       ├── EventRequest.java
│       └── RegistrationResponse.java
│
├── company/                                # Company Module (✅ Already good)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
│
├── voucher/                                # Voucher Module (✅ Already good)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   └── dto/
│
├── storage/                                # File Storage Module
│   ├── controller/
│   │   └── StorageController.java
│   └── service/
│       └── StorageService.java
│
├── security/                               # Security Infrastructure
│   ├── jwt/
│   │   ├── JwtAuthFilter.java
│   │   └── JwtService.java
│   ├── oauth2/
│   │   └── CustomOAuth2UserService.java
│   └── config/
│       └── SecurityConfig.java
│
├── config/                                 # Application Configuration
│   ├── OpenAPIConfig.java
│   └── JpaAuditingConfig.java
│
└── exception/                              # Exception Handling
    ├── handler/
    │   ├── GlobalExceptionHandler.java
    │   └── ErrorResponse.java
    ├── auth/
    │   ├── InvalidPasswordResetTokenException.java
    │   └── EmailAlreadyExistsException.java
    ├── user/
    │   └── UserNotFoundException.java
    ├── event/
    │   └── EventNotFoundException.java
    ├── company/
    │   └── CompanyNotFoundException.java
    ├── voucher/
    │   ├── VoucherNotFoundException.java
    │   └── InsufficientPointsException.java
    ├── storage/
    │   ├── FileStorageException.java
    │   └── InvalidFilePathException.java
    └── common/
        └── ResourceNotFoundException.java
```

## How to Refactor (Using IntelliJ IDEA)

### Step 1: Prepare
1. Commit all current changes
2. Create a new branch: `git checkout -b refactor/improve-structure`
3. Ensure all tests pass (if available)

### Step 2: Create Module Packages
```bash
# Run this script to create the structure
./create-module-structure.sh
```

### Step 3: Move Files Using IDE (Preserves imports)

**For Auth Module:**
1. Right-click `controller/AuthController.java`
2. Refactor → Move
3. Select `com.multi.loyaltybackend.auth.controller`
4. Click "Refactor" - IDE will update all imports automatically

**Repeat for:**
- `service/AuthService.java` → `auth.service`
- `service/EmailService.java` → `auth.service`
- Move DTOs from `dto/` → `auth.dto/`

**For User Module:**
- `controller/ProfileController.java` → `user.controller.UserController`
- `service/ProfileService.java` → `user.service.UserService`
- `repository/UserRepository.java` → `user.repository`
- `model/User.java` → `user.model`
- `model/Role.java` → `user.model`

**For Event Module:**
- `controller/EventController.java` → `event.controller`
- `service/EventService.java` → `event.service`
- `service/RegistrationService.java` → `event.service`
- `repository/EventRepository.java` → `event.repository`
- `repository/RegistrationRepository.java` → `event.repository`
- `model/Event.java` → `event.model`
- `model/Registration.java` → `event.model`
- `model/EventCategory.java` → `event.model`
- `model/RegistrationStatus.java` → `event.model`

**For Storage Module:**
- `controller/ImageStorageController.java` → `storage.controller.StorageController`
- `service/ImageStorageService.java` → `storage.service.StorageService`

**For Security:**
- `config/JwtAuthFilter.java` → `security.jwt`
- `service/JwtService.java` → `security.jwt`
- `service/CustomOAuth2UserService.java` → `security.oauth2`
- `config/SecurityConfig.java` → `security.config`

**For Exceptions:**
- Organize by domain as shown in structure above

### Step 4: Update References
1. IDE should auto-update most imports
2. Search for any remaining import errors
3. Update `@ComponentScan` if using custom scanning

### Step 5: Test
```bash
# Compile
mvn clean compile

# Run tests (if available)
mvn test

# Run application
mvn spring-boot:run
```

### Step 6: Commit
```bash
git add -A
git commit -m "Refactor: Organize code into feature-based modules"
git push -u origin refactor/improve-structure
```

## Benefits

1. **Feature-Based Organization**
   - Each business domain (auth, user, event, etc.) in its own package
   - Easy to find related code
   - Clear module boundaries

2. **Consistent Structure**
   - Every module follows the same pattern
   - New developers can navigate easily
   - Predictable file locations

3. **Separation of Concerns**
   - Security infrastructure separated
   - Exceptions organized by domain
   - Configuration centralized

4. **Scalability**
   - Easy to add new modules
   - Can extract modules to microservices later
   - Clear dependencies between modules

5. **Maintainability**
   - Related code stays together
   - Easier to refactor individual modules
   - Reduced cognitive load

## Alternative: Gradual Refactoring

If full refactoring is too risky, do it gradually:

### Phase 1: Organize Exceptions (Low Risk)
- Create exception subdirectories by domain
- Move exception classes
- Update imports in GlobalExceptionHandler

### Phase 2: Security Package (Medium Risk)
- Create `security/` package
- Move JWT and OAuth2 classes
- Update SecurityConfig imports

### Phase 3: Feature Modules (Higher Risk)
- One module at a time: auth → user → event → storage
- Test after each module
- Can roll back if issues arise

## Naming Conventions

### Before → After
- `ProfileController` → `UserController`
- `ProfileService` → `UserService`
- `ImageStorageController` → `StorageController`
- `ImageStorageService` → `StorageService`

### DTO Naming
- Use specific names: `LoginRequest`, `LoginResponse`
- Not generic: `AuthRequest`, `AuthResponse`
- Include purpose in name

### Exception Naming
- Domain-specific: `VoucherNotFoundException`
- Not generic: `NotFoundException`
- Organize by related domain

## Tools to Help

1. **IntelliJ IDEA**
   - Refactor → Move Class
   - Refactor → Rename
   - Analyze → Inspect Code

2. **Eclipse**
   - Refactor → Move
   - Refactor → Rename

3. **VS Code**
   - Use Java Extension Pack
   - Right-click → Refactor

## Validation Checklist

After refactoring:
- [ ] Application compiles without errors
- [ ] All tests pass
- [ ] Application starts successfully
- [ ] Swagger UI loads correctly
- [ ] Can login and get JWT token
- [ ] Can access protected endpoints
- [ ] File upload/download works
- [ ] OAuth2 login works
- [ ] All endpoints return correct responses

## Common Issues

1. **Circular Dependencies**
   - If modules depend on each other, consider extracting common code
   - Use interfaces to break cycles

2. **Bean Not Found**
   - Ensure @ComponentScan includes new packages
   - Check @SpringBootApplication location

3. **Import Errors**
   - Let IDE auto-fix imports
   - Search for old package names

4. **Test Failures**
   - Update test imports
   - Update @ContextConfiguration if using custom config

## Support

If you encounter issues during refactoring:
1. Check this guide
2. Use IDE refactoring tools (not manual editing)
3. Test after each major change
4. Keep commits small and focused
5. Can always rollback with `git reset --hard`
