# Loyalty Backend API

A comprehensive loyalty management system built with Spring Boot that enables organizations to manage user engagement through events, rewards, and voucher systems.

## Features

- **User Management**: Registration, authentication, profile management
- **Event System**: Create and manage events, track user registrations, award points
- **Company Management**: Manage companies with logo uploads
- **Voucher System**: Create vouchers, track exchanges, validate expiry and points
- **Points & Rewards**: Automatic points awarding for event participation
- **Security**: JWT-based authentication, OAuth2 (Google) integration, password reset
- **API Documentation**: Interactive Swagger/OpenAPI documentation

## Tech Stack

- **Java 21**
- **Spring Boot 3.3.5**
- **Spring Security** with JWT and OAuth2
- **Spring Data JPA** with Hibernate
- **H2 Database** (development)
- **Maven**
- **Lombok**
- **Swagger/OpenAPI 3.0**

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Git

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd loyalty-backend
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory (you can copy from `.env.example`):

```bash
cp .env.example .env
```

Edit `.env` and set your values:

```properties
# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-here-make-it-long-and-random
JWT_EXPIRATION_MS=86400000

# Email (SMTP) Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Google OAuth2 Client Configuration
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Application URLs
APP_FRONTEND_URL=http://localhost:3000
APP_SERVER_URL=http://localhost:8080

# Development Settings (optional)
H2_CONSOLE_ENABLED=true
SQL_SHOW_SQL=true
SQL_LOG_LEVEL=DEBUG
```

### 3. Set Environment Variables

**Linux/MacOS:**
```bash
export $(cat .env | xargs)
```

**Windows (PowerShell):**
```powershell
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    Set-Item -Path "env:$name" -Value $value
}
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Database

### H2 Console (Development)

Access the H2 database console at: http://localhost:8080/h2-console

- **JDBC URL**: `jdbc:h2:mem:default`
- **Username**: `sa`
- **Password**: (leave empty)

### Production Database

For production, replace H2 with a production-ready database:

1. Add database dependency to `pom.xml` (PostgreSQL, MySQL, etc.)
2. Update `application.properties` with production database configuration
3. Update environment variables with production database credentials

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/logout` - Logout (invalidate token)
- `POST /api/auth/forget-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token
- `GET /oauth2/authorization/google` - Google OAuth2 login

### Users
- `GET /api/profile` - Get current user profile
- `PUT /api/profile` - Update user profile
- `POST /api/profile/upload-image` - Upload profile image

### Events
- `GET /api/events` - Get all events (with pagination)
- `GET /api/events/{id}` - Get event by ID
- `POST /api/events` - Create new event
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event
- `POST /api/events/register` - Register user to event

### Companies
- `GET /api/companies` - Get all companies (with pagination)
- `GET /api/companies/{id}` - Get company by ID
- `POST /api/companies` - Create new company
- `PUT /api/companies/{id}` - Update company
- `DELETE /api/companies/{id}` - Delete company

### Vouchers
- `GET /api/vouchers` - Get all vouchers (with pagination)
- `GET /api/vouchers/{id}` - Get voucher by ID
- `POST /api/vouchers` - Create new voucher
- `PUT /api/vouchers/{id}` - Update voucher
- `DELETE /api/vouchers/{id}` - Delete voucher
- `POST /api/vouchers/exchange` - Exchange voucher for points

## Authentication

### JWT Authentication

Include the JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### OAuth2 Authentication

The application supports Google OAuth2. Configure your Google OAuth2 credentials in the `.env` file.

## Security Features

- **JWT Token Management**: Secure token generation and validation with blacklist support
- **Password Encryption**: BCrypt password hashing
- **Password Reset**: Token-based password reset with expiration (1 hour)
- **Path Traversal Protection**: Secure file upload/download
- **Thread-Safe Token Blacklist**: Concurrent-safe logout mechanism
- **Method-Level Security**: `@PreAuthorize` annotations support
- **CORS Configuration**: Configurable cross-origin requests

## Error Handling

The API returns standardized error responses:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Company not found with id: 123",
  "path": "/api/companies/123"
}
```

## Improvements Implemented

This version includes significant improvements over the initial codebase:

### Security Enhancements
- ✅ Environment-based configuration (no hardcoded credentials)
- ✅ Proper authentication enforcement on all endpoints
- ✅ Thread-safe token blacklist
- ✅ Password reset token expiration
- ✅ Path traversal protection
- ✅ OAuth2 security fixes

### Code Quality
- ✅ Custom exception classes for better error handling
- ✅ Specific HTTP status codes (404, 409, 400, etc.)
- ✅ Input validation on all endpoints
- ✅ Consistent exception handling

### Features
- ✅ Event points awarding on registration
- ✅ Voucher expiry validation
- ✅ Pagination support for list endpoints
- ✅ Lazy loading for better performance
- ✅ Swagger/OpenAPI documentation

### Performance
- ✅ Fixed N+1 query issues
- ✅ Changed eager loading to lazy loading
- ✅ Optimized database queries

## Development

### Project Structure

```
src/main/java/com/multi/loyaltybackend/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── service/         # Business logic
├── repository/      # Data access layer
├── model/          # Entity classes
├── dto/            # Data transfer objects
├── exception/      # Custom exceptions
├── company/        # Company module
└── voucher/        # Voucher module
```

### Best Practices

1. **Never commit sensitive data** - Use environment variables
2. **Use pagination** for list endpoints
3. **Use custom exceptions** instead of generic RuntimeException
4. **Add proper logging** for debugging and monitoring
5. **Write tests** for critical business logic
6. **Document APIs** using Swagger annotations

## Production Deployment

Before deploying to production:

1. ✅ Set strong, unique `JWT_SECRET`
2. ✅ Configure production database (PostgreSQL/MySQL)
3. ✅ Set `H2_CONSOLE_ENABLED=false`
4. ✅ Set `SQL_SHOW_SQL=false`
5. ✅ Set `SQL_LOG_LEVEL=INFO` or `WARN`
6. ✅ Use HTTPS for all endpoints
7. ✅ Configure CORS properly
8. ✅ Set up monitoring and logging
9. ✅ Implement rate limiting
10. ✅ Regular security audits

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

## License

[Specify your license here]

## Support

For issues and questions, please open an issue in the repository.

---

**Built with ❤️ using Spring Boot**
