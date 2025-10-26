package com.multi.loyaltybackend.chat.controller;

import com.multi.loyaltybackend.chat.dto.*;
import com.multi.loyaltybackend.chat.service.ChatService;
import com.multi.loyaltybackend.dto.ApiResponse;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for support chat operations.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Support chat management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    /**
     * Create a new support chat
     */
    @PostMapping
    @Operation(summary = "Create a new support chat", description = "Allows users to initiate a support chat")
    public ApiResponse<ChatResponse> createChat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatCreateRequest request
    ) {
        ChatResponse chat = chatService.createChat(user.getId(), request);
        return ResponseUtil.success(chat, "Chat created successfully", HttpStatus.CREATED);
    }

    /**
     * Get all chats for the authenticated user
     */
    @GetMapping
    @Operation(summary = "Get user's chats", description = "Retrieve all chats for the authenticated user")
    public ApiResponse<List<ChatResponse>> getUserChats(
            @AuthenticationPrincipal User user,
            @ModelAttribute ChatFilterDTO filters
    ) {
        List<ChatResponse> chats = chatService.getUserChats(user.getId(), filters);
        return ResponseUtil.success(chats);
    }

    /**
     * Get a specific chat
     */
    @GetMapping("/{chatId}")
    @Operation(summary = "Get chat details", description = "Retrieve details of a specific chat")
    public ApiResponse<ChatResponse> getChat(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId
    ) {
        ChatResponse chat = chatService.getChat(user.getId(), chatId);
        return ResponseUtil.success(chat);
    }

    /**
     * Get messages for a chat
     */
    @GetMapping("/{chatId}/messages")
    @Operation(summary = "Get chat messages", description = "Retrieve paginated messages for a chat")
    public ApiResponse<Page<MessageResponse>> getMessages(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<MessageResponse> messages = chatService.getMessages(user.getId(), chatId, pageable);
        return ResponseUtil.success(messages);
    }

    /**
     * Send a message in a chat
     */
    @PostMapping(value = "/{chatId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Send a message", description = "Send a message with optional file attachment")
    public ApiResponse<MessageResponse> sendMessage(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId,
            @RequestPart("message") @Valid MessageSendRequest request,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment
    ) {
        request.setChatId(chatId);
        MessageResponse message = chatService.sendMessage(user.getId(), request, attachment);
        return ResponseUtil.success(message, "Message sent successfully", HttpStatus.CREATED);
    }

    /**
     * Send a text-only message (alternative endpoint)
     */
    @PostMapping("/{chatId}/messages/text")
    @Operation(summary = "Send a text message", description = "Send a text-only message")
    public ApiResponse<MessageResponse> sendTextMessage(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId,
            @Valid @RequestBody MessageSendRequest request
    ) {
        request.setChatId(chatId);
        MessageResponse message = chatService.sendMessage(user.getId(), request, null);
        return ResponseUtil.success(message, "Message sent successfully", HttpStatus.CREATED);
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/{chatId}/read")
    @Operation(summary = "Mark messages as read", description = "Mark all unread messages in a chat as read")
    public ApiResponse<String> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId
    ) {
        chatService.markMessagesAsRead(user.getId(), chatId);
        return ResponseUtil.success("Messages marked as read");
    }

    /**
     * Close a chat
     */
    @PutMapping("/{chatId}/close")
    @Operation(summary = "Close a chat", description = "Close a support chat")
    public ApiResponse<ChatResponse> closeChat(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId
    ) {
        ChatResponse chat = chatService.closeChat(user.getId(), chatId);
        return ResponseUtil.success(chat, "Chat closed successfully");
    }

    /**
     * Delete a chat
     */
    @DeleteMapping("/{chatId}")
    @Operation(summary = "Delete a chat", description = "Delete a chat and all its messages")
    public ApiResponse<String> deleteChat(
            @AuthenticationPrincipal User user,
            @PathVariable Long chatId
    ) {
        chatService.deleteChat(user.getId(), chatId);
        return ResponseUtil.success("Chat deleted successfully");
    }

    // Admin endpoints

    /**
     * Get all chats (admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all chats (Admin)", description = "Retrieve all support chats with filters")
    public ApiResponse<List<ChatResponse>> getAllChats(
            @ModelAttribute ChatFilterDTO filters
    ) {
        List<ChatResponse> chats = chatService.getAllChats(filters);
        return ResponseUtil.success(chats);
    }

    /**
     * Assign chat to an agent (admin only)
     */
    @PutMapping("/admin/{chatId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign chat to agent (Admin)", description = "Assign a support chat to a specific agent")
    public ApiResponse<ChatResponse> assignChat(
            @PathVariable Long chatId,
            @Valid @RequestBody ChatAssignRequest request
    ) {
        ChatResponse chat = chatService.assignChat(chatId, request.getAgentId());
        return ResponseUtil.success(chat, "Chat assigned successfully");
    }
}
