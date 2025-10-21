/**
 * Voucher Management Module.
 * <p>
 * This module handles voucher creation, exchange, and tracking. It implements the core
 * loyalty reward system where users can exchange points for vouchers.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Create and manage vouchers</li>
 *   <li>Exchange points for vouchers</li>
 *   <li>Track voucher usage and status</li>
 *   <li>Validate voucher expiry dates</li>
 *   <li>Prevent duplicate voucher exchanges</li>
 *   <li>Check user point sufficiency</li>
 * </ul>
 *
 * <h2>Module Structure</h2>
 * <p>
 * This is a well-organized module following best practices:
 * <pre>
 * voucher/
 * ├── controller/  - REST API endpoints
 * ├── service/     - Business logic
 * ├── repository/  - Data access
 * ├── model/       - Domain entities
 * └── dto/         - Data transfer objects
 * </pre>
 *
 * <h2>Domain Model</h2>
 * <ul>
 *   <li>{@link com.multi.loyaltybackend.voucher.model.Voucher} - Voucher definition</li>
 *   <li>{@link com.multi.loyaltybackend.voucher.model.UserVoucher} - User-voucher association</li>
 *   <li>{@link com.multi.loyaltybackend.model.VoucherStatus} - Voucher status (ACTIVE, USED, EXPIRED)</li>
 * </ul>
 *
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Users must have sufficient points to exchange</li>
 *   <li>Vouchers must not be expired</li>
 *   <li>Users cannot exchange the same voucher twice</li>
 *   <li>Points are deducted immediately upon exchange</li>
 * </ul>
 *
 * <h2>API Endpoints</h2>
 * <ul>
 *   <li>{@code GET /api/vouchers} - List all vouchers (paginated)</li>
 *   <li>{@code GET /api/vouchers/{id}} - Get voucher by ID</li>
 *   <li>{@code POST /api/vouchers} - Create new voucher</li>
 *   <li>{@code PUT /api/vouchers/{id}} - Update voucher</li>
 *   <li>{@code DELETE /api/vouchers/{id}} - Delete voucher</li>
 *   <li>{@code POST /api/vouchers/exchange} - Exchange points for voucher</li>
 * </ul>
 *
 * <p>
 * <strong>Example:</strong> This module demonstrates the recommended structure for all modules.
 *
 * @see com.multi.loyaltybackend.voucher.controller.VoucherController
 * @see com.multi.loyaltybackend.voucher.service.VoucherService
 * @since 1.0.0
 */
package com.multi.loyaltybackend.voucher;
