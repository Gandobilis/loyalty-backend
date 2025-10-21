/**
 * Business Logic Services for the Loyalty Backend Application.
 * <p>
 * This package contains service classes that implement core business logic and orchestrate
 * operations between controllers and repositories. Services are annotated with {@code @Service}
 * and are transactional where appropriate.
 *
 * <h2>Services</h2>
 * <ul>
 *   <li>{@link com.multi.loyaltybackend.service.AuthService} - Authentication and user registration</li>
 *   <li>{@link com.multi.loyaltybackend.service.ProfileService} - User profile operations</li>
 *   <li>{@link com.multi.loyaltybackend.service.EventService} - Event management</li>
 *   <li>{@link com.multi.loyaltybackend.service.RegistrationService} - Event registration handling</li>
 *   <li>{@link com.multi.loyaltybackend.service.JwtService} - JWT token generation and validation</li>
 *   <li>{@link com.multi.loyaltybackend.service.EmailService} - Email notifications</li>
 *   <li>{@link com.multi.loyaltybackend.service.ImageStorageService} - File storage operations</li>
 *   <li>{@link com.multi.loyaltybackend.service.CustomOAuth2UserService} - OAuth2 user handling</li>
 * </ul>
 *
 * <h2>Transaction Management</h2>
 * <p>
 * Methods that modify data should use {@code @Transactional} to ensure data consistency.
 * Read-only operations should use {@code @Transactional(readOnly = true)} for optimization.
 *
 * <h2>Exception Handling</h2>
 * <p>
 * Services throw domain-specific exceptions (e.g., {@code UserNotFoundException},
 * {@code InsufficientPointsException}) which are caught and handled by
 * {@link com.multi.loyaltybackend.exception.GlobalExceptionHandler}.
 *
 * <p>
 * <strong>Note:</strong> This package is recommended to be refactored into feature-based modules.
 * See {@code STRUCTURE_IMPROVEMENT_GUIDE.md} for details.
 *
 * @since 1.0.0
 */
package com.multi.loyaltybackend.service;
