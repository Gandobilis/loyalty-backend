/**
 * Configuration Classes for the Loyalty Backend Application.
 * <p>
 * This package contains Spring configuration classes that set up various aspects
 * of the application including security, API documentation, and infrastructure.
 *
 * <h2>Configuration Classes</h2>
 * <ul>
 *   <li>{@link com.multi.loyaltybackend.config.SecurityConfig} - Security and authentication setup</li>
 *   <li>{@link com.multi.loyaltybackend.config.OpenAPIConfig} - Swagger/OpenAPI documentation</li>
 *   <li>{@link com.multi.loyaltybackend.config.JwtAuthFilter} - JWT token validation filter</li>
 * </ul>
 *
 * <h2>Security Configuration</h2>
 * <p>
 * {@link com.multi.loyaltybackend.config.SecurityConfig} configures:
 * <ul>
 *   <li>JWT-based authentication</li>
 *   <li>OAuth2 login (Google)</li>
 *   <li>Password encoding (BCrypt)</li>
 *   <li>Endpoint authorization rules</li>
 *   <li>CORS settings</li>
 * </ul>
 *
 * <h2>Public Endpoints</h2>
 * <p>
 * The following endpoints do not require authentication:
 * <ul>
 *   <li>{@code /api/auth/register}</li>
 *   <li>{@code /api/auth/login}</li>
 *   <li>{@code /api/auth/forget-password}</li>
 *   <li>{@code /api/images/**}</li>
 *   <li>{@code /swagger-ui/**}</li>
 *   <li>{@code /v3/api-docs/**}</li>
 *   <li>{@code /h2-console/**} (development only)</li>
 * </ul>
 *
 * <h2>API Documentation</h2>
 * <p>
 * {@link com.multi.loyaltybackend.config.OpenAPIConfig} sets up Swagger UI
 * with JWT authentication support. Access at:
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 *
 * <h2>Recommended Refactoring</h2>
 * <p>
 * Security-related classes should be moved to a dedicated security package:
 * <pre>
 * security/
 * ├── config/
 * │   └── SecurityConfig.java
 * ├── jwt/
 * │   ├── JwtAuthFilter.java
 * │   └── JwtService.java
 * └── oauth2/
 *     └── CustomOAuth2UserService.java
 * </pre>
 *
 * @see org.springframework.context.annotation.Configuration
 * @since 1.0.0
 */
package com.multi.loyaltybackend.config;
