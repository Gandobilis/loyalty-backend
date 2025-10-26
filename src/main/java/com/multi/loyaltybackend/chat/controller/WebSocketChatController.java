package com.multi.loyaltybackend.chat.controller;

import com.multi.loyaltybackend.chat.dto.MessageSendRequest;
import com.multi.loyaltybackend.chat.dto.WebSocketMessage;
import com.multi.loyaltybackend.chat.service.ChatService;
import com.multi.loyaltybackend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket controller for real-time chat messaging.
 *
 * Message Flows:
 * 1. Client connects to /ws with SockJS
 * 2. Client subscribes to /topic/chat/{chatId} to receive messages
 * 3. Client sends messages to /app/chat/{chatId}/send
 * 4. Server broadcasts messages to all subscribers on /topic/chat/{chatId}
 *
 * Additional Features:
 * - Typing indicators: /app/chat/{chatId}/typing
 * - Read receipts: /app/chat/{chatId}/read
 * - User presence: /app/chat/{chatId}/join and /leave
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages
     * Client sends to: /app/chat/{chatId}/send
     * Server broadcasts to: /topic/chat/{chatId}
     */
    @MessageMapping("/chat/{chatId}/send")
    @SendTo("/topic/chat/{chatId}")
    public WebSocketMessage sendMessage(
            @DestinationVariable Long chatId,
            @Payload MessageSendRequest request,
            @AuthenticationPrincipal User user
    ) {
        try {
            request.setChatId(chatId);
            var messageResponse = chatService.sendMessage(user.getId(), request, null);

            return WebSocketMessage.builder()
                    .type("MESSAGE")
                    .chatId(chatId)
                    .messageId(messageResponse.getId())
                    .senderId(user.getId())
                    .senderFullName(user.getFullName())
                    .content(messageResponse.getContent())
                    .messageType(messageResponse.getMessageType())
                    .attachmentUrl(messageResponse.getAttachmentUrl())
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error sending WebSocket message", e);
            return WebSocketMessage.builder()
                    .type("ERROR")
                    .chatId(chatId)
                    .content("Failed to send message: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Handle typing indicators
     * Client sends to: /app/chat/{chatId}/typing
     * Server broadcasts to: /topic/chat/{chatId}/typing
     */
    @MessageMapping("/chat/{chatId}/typing")
    public void handleTyping(
            @DestinationVariable Long chatId,
            @AuthenticationPrincipal User user
    ) {
        WebSocketMessage typingMessage = WebSocketMessage.builder()
                .type("TYPING")
                .chatId(chatId)
                .senderId(user.getId())
                .senderFullName(user.getFullName())
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", typingMessage);
    }

    /**
     * Handle read receipts
     * Client sends to: /app/chat/{chatId}/read
     * Server broadcasts to: /topic/chat/{chatId}/read
     */
    @MessageMapping("/chat/{chatId}/read")
    public void handleRead(
            @DestinationVariable Long chatId,
            @AuthenticationPrincipal User user
    ) {
        try {
            chatService.markMessagesAsRead(user.getId(), chatId);

            WebSocketMessage readMessage = WebSocketMessage.builder()
                    .type("READ")
                    .chatId(chatId)
                    .senderId(user.getId())
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/read", readMessage);
        } catch (Exception e) {
            log.error("Error marking messages as read", e);
        }
    }

    /**
     * Handle user joining a chat
     * Client sends to: /app/chat/{chatId}/join
     * Server broadcasts to: /topic/chat/{chatId}/presence
     */
    @MessageMapping("/chat/{chatId}/join")
    public void handleJoin(
            @DestinationVariable Long chatId,
            @AuthenticationPrincipal User user
    ) {
        WebSocketMessage joinMessage = WebSocketMessage.builder()
                .type("JOINED")
                .chatId(chatId)
                .senderId(user.getId())
                .senderFullName(user.getFullName())
                .content(user.getFullName() + " joined the chat")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/presence", joinMessage);
        log.info("User {} joined chat {}", user.getId(), chatId);
    }

    /**
     * Handle user leaving a chat
     * Client sends to: /app/chat/{chatId}/leave
     * Server broadcasts to: /topic/chat/{chatId}/presence
     */
    @MessageMapping("/chat/{chatId}/leave")
    public void handleLeave(
            @DestinationVariable Long chatId,
            @AuthenticationPrincipal User user
    ) {
        WebSocketMessage leaveMessage = WebSocketMessage.builder()
                .type("LEFT")
                .chatId(chatId)
                .senderId(user.getId())
                .senderFullName(user.getFullName())
                .content(user.getFullName() + " left the chat")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/presence", leaveMessage);
        log.info("User {} left chat {}", user.getId(), chatId);
    }

    /**
     * Handle chat subscription
     * Client subscribes to: /topic/chat/{chatId}
     */
    @SubscribeMapping("/chat/{chatId}")
    public WebSocketMessage handleSubscribe(
            @DestinationVariable Long chatId,
            @AuthenticationPrincipal User user
    ) {
        log.info("User {} subscribed to chat {}", user.getId(), chatId);

        return WebSocketMessage.builder()
                .type("SUBSCRIBED")
                .chatId(chatId)
                .content("Successfully subscribed to chat " + chatId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
