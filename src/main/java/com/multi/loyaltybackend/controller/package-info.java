/**
 * REST API Controllers for the Loyalty Backend Application.
 * <p>
 * This package contains HTTP endpoint controllers that handle client requests and return responses.
 * All controllers use Spring's {@code @RestController} annotation and return data in JSON format.
 *
 * <h2>Controllers</h2>
 * <ul>
 *   <li>{@link com.multi.loyaltybackend.controller.AuthController} - Authentication endpoints (login, register, password reset)</li>
 *   <li>{@link com.multi.loyaltybackend.controller.ProfileController} - User profile management</li>
 *   <li>{@link com.multi.loyaltybackend.controller.EventController} - Event management and registrations</li>
 *   <li>{@link com.multi.loyaltybackend.controller.ImageStorageController} - File upload/download endpoints</li>
 * </ul>
 *
 * <h2>API Documentation</h2>
 * <p>
 * Interactive API documentation is available at:
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code /v3/api-docs}</li>
 * </ul>
 *
 * <h2>Security</h2>
 * <p>
 * Most endpoints require JWT authentication. Include the token in the Authorization header:
 * <pre>
 * Authorization: Bearer &lt;your-jwt-token&gt;
 * </pre>
 *
 * <p>
 * <strong>Note:</strong> This package is recommended to be refactored into feature-based modules.
 * See {@code STRUCTURE_IMPROVEMENT_GUIDE.md} for details.
 *
 * @see com.multi.loyaltybackend.config.SecurityConfig
 * @since 1.0.0
 */
package com.multi.loyaltybackend.controller;
