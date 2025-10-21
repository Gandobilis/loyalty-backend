package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    /**
     * Get dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(
                ApiResponse.<DashboardStatsDTO>builder()
                        .success(true)
                        .message("Dashboard statistics retrieved successfully")
                        .data(stats)
                        .build()
        );
    }

    /**
     * Get all users
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserManagementDTO>>> getAllUsers() {
        List<UserManagementDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.<List<UserManagementDTO>>builder()
                        .success(true)
                        .message("Users retrieved successfully")
                        .data(users)
                        .build()
        );
    }

    /**
     * Get user by ID
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserManagementDTO>> getUserById(@PathVariable Long id) {
        UserManagementDTO user = adminService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.<UserManagementDTO>builder()
                        .success(true)
                        .message("User retrieved successfully")
                        .data(user)
                        .build()
        );
    }

    /**
     * Update user role
     * PATCH /api/admin/users/{id}/role
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserManagementDTO>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserManagementDTO updatedUser = adminService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(
                ApiResponse.<UserManagementDTO>builder()
                        .success(true)
                        .message("User role updated successfully")
                        .data(updatedUser)
                        .build()
        );
    }

    /**
     * Update user points (add or subtract)
     * PATCH /api/admin/users/{id}/points
     */
    @PatchMapping("/users/{id}/points")
    public ResponseEntity<ApiResponse<UserManagementDTO>> updateUserPoints(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPointsRequest request) {
        UserManagementDTO updatedUser = adminService.updateUserPoints(id, request.getPoints());
        return ResponseEntity.ok(
                ApiResponse.<UserManagementDTO>builder()
                        .success(true)
                        .message("User points updated successfully")
                        .data(updatedUser)
                        .build()
        );
    }

    /**
     * Delete user
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User deleted successfully")
                        .build()
        );
    }
}
