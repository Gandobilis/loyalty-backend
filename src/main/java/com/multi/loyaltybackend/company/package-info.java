/**
 * Company Management Module.
 * <p>
 * This module handles all company-related functionality including company profiles,
 * logo management, and company-voucher relationships.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Create, read, update, and delete companies</li>
 *   <li>Upload and manage company logos</li>
 *   <li>Link companies to vouchers</li>
 *   <li>Pagination support for company listings</li>
 * </ul>
 *
 * <h2>Module Structure</h2>
 * <p>
 * This is a well-organized module following best practices:
 * <pre>
 * company/
 * ├── controller/  - REST API endpoints
 * ├── service/     - Business logic
 * ├── repository/  - Data access
 * └── model/       - Domain entities
 * </pre>
 *
 * <h2>API Endpoints</h2>
 * <ul>
 *   <li>{@code GET /api/companies} - List all companies (paginated)</li>
 *   <li>{@code GET /api/companies/{id}} - Get company by ID</li>
 *   <li>{@code POST /api/companies} - Create new company</li>
 *   <li>{@code PUT /api/companies/{id}} - Update company</li>
 *   <li>{@code DELETE /api/companies/{id}} - Delete company</li>
 * </ul>
 *
 * <p>
 * <strong>Example:</strong> This module demonstrates the recommended structure for all modules.
 *
 * @see com.multi.loyaltybackend.company.controller.CompanyController
 * @see com.multi.loyaltybackend.company.service.CompanyService
 * @since 1.0.0
 */
package com.multi.loyaltybackend.company;
