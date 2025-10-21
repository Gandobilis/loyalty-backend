/**
 * Root package for the Loyalty Backend Application.
 * <p>
 * This application provides a comprehensive loyalty management system with the following features:
 * <ul>
 *   <li>User authentication and profile management</li>
 *   <li>Event management and user registrations</li>
 *   <li>Company and voucher management</li>
 *   <li>Points-based reward system</li>
 *   <li>File storage for images and attachments</li>
 * </ul>
 *
 * <h2>Package Organization</h2>
 * <p>
 * The application is organized into functional modules:
 * <ul>
 *   <li>{@link com.multi.loyaltybackend.company} - Company management</li>
 *   <li>{@link com.multi.loyaltybackend.voucher} - Voucher and exchange system</li>
 *   <li>{@link com.multi.loyaltybackend.controller} - REST API controllers</li>
 *   <li>{@link com.multi.loyaltybackend.service} - Business logic services</li>
 *   <li>{@link com.multi.loyaltybackend.repository} - Data access layer</li>
 *   <li>{@link com.multi.loyaltybackend.model} - Domain entities</li>
 *   <li>{@link com.multi.loyaltybackend.config} - Application configuration</li>
 *   <li>{@link com.multi.loyaltybackend.exception} - Exception handling</li>
 * </ul>
 *
 * <h2>Security</h2>
 * <p>
 * The application uses JWT-based authentication with support for:
 * <ul>
 *   <li>Traditional username/password login</li>
 *   <li>OAuth2 authentication (Google)</li>
 *   <li>Password reset via email</li>
 * </ul>
 *
 * @see com.multi.loyaltybackend.LoyaltyBackendApplication
 * @version 1.0.0
 * @since 1.0.0
 */
package com.multi.loyaltybackend;
