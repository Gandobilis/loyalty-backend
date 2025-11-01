package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.CreateSupportMessageRequest;
import com.multi.loyaltybackend.dto.RespondToSupportMessageRequest;
import com.multi.loyaltybackend.dto.SupportMessageResponse;
import com.multi.loyaltybackend.model.SupportMessageStatus;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.service.SupportMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Support Messages", description = "Support message management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SupportMessageController {

    private final SupportMessageService supportMessageService;

    // ==================== USER ENDPOINTS ====================

    @PostMapping("/api/support/messages")
    @Operation(summary = "Create a support message", description = "User can create a new support message")
    public ResponseEntity<SupportMessageResponse> createMessage(
            @Valid @RequestBody CreateSupportMessageRequest request,
            @AuthenticationPrincipal User user) {
        SupportMessageResponse response = supportMessageService.createMessage(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/support/messages")
    @Operation(summary = "Get user's support messages", description = "User can view their own support messages")
    public ResponseEntity<Page<SupportMessageResponse>> getUserMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        Page<SupportMessageResponse> messages = supportMessageService.getUserMessages(user, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/api/support/messages/{id}")
    @Operation(summary = "Get support message by ID", description = "User can view their specific support message")
    public ResponseEntity<SupportMessageResponse> getMessageById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        SupportMessageResponse message = supportMessageService.getMessageById(id, user);
        return ResponseEntity.ok(message);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/support/messages")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all support messages (Admin)", description = "Admin can view all support messages with optional filtering")
    public ResponseEntity<Page<SupportMessageResponse>> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) SupportMessageStatus status) {
        Page<SupportMessageResponse> messages = supportMessageService.getAllMessages(page, size, status);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/support/messages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get support message by ID (Admin)", description = "Admin can view any support message")
    public ResponseEntity<SupportMessageResponse> getMessageByIdAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        SupportMessageResponse message = supportMessageService.getMessageById(id, admin);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/admin/support/messages/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Respond to support message (Admin)", description = "Admin can respond to a support message")
    public ResponseEntity<SupportMessageResponse> respondToMessage(
            @PathVariable Long id,
            @Valid @RequestBody RespondToSupportMessageRequest request,
            @AuthenticationPrincipal User admin) {
        SupportMessageResponse response = supportMessageService.respondToMessage(id, request, admin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/admin/support/messages/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update message status (Admin)", description = "Admin can update the status of a support message")
    public ResponseEntity<SupportMessageResponse> updateMessageStatus(
            @PathVariable Long id,
            @RequestParam SupportMessageStatus status,
            @AuthenticationPrincipal User admin) {
        SupportMessageResponse response = supportMessageService.updateMessageStatus(id, status, admin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/support/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get support message statistics (Admin)", description = "Admin can view statistics about support messages")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("open", supportMessageService.countMessagesByStatus(SupportMessageStatus.OPEN));
        statistics.put("inProgress", supportMessageService.countMessagesByStatus(SupportMessageStatus.IN_PROGRESS));
        statistics.put("resolved", supportMessageService.countMessagesByStatus(SupportMessageStatus.RESOLVED));
        statistics.put("closed", supportMessageService.countMessagesByStatus(SupportMessageStatus.CLOSED));
        return ResponseEntity.ok(statistics);
    }
}
