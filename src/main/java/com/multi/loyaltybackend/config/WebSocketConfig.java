package com.multi.loyaltybackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time chat messaging.
 *
 * This configuration enables STOMP over WebSocket for bidirectional communication.
 *
 * Endpoints:
 * - /ws: WebSocket connection endpoint with SockJS fallback
 *
 * Message Destinations:
 * - /app/*: Application-level destinations (messages sent from client to server)
 * - /topic/*: Broadcast destinations (server broadcasts to all subscribers)
 * - /queue/*: Point-to-point destinations (server sends to specific user)
 *
 * Example usage from client:
 * - Connect: stompClient.connect({}, onConnected)
 * - Subscribe: stompClient.subscribe('/topic/chat/123', onMessageReceived)
 * - Send: stompClient.send('/app/chat/send', {}, JSON.stringify(message))
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory message broker
        // Prefix for messages that are routed to message-handling methods
        registry.setApplicationDestinationPrefixes("/app");

        // Enable simple broker for pub/sub messaging
        // Messages sent to destinations starting with /topic or /queue will be routed to broker
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Configure allowed origins
                .withSockJS(); // Enable SockJS fallback for browsers that don't support WebSocket
    }
}
