package com.experimental.anonchat.configurations;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enables an in-memory message broker with support for user destinations
        registry.enableSimpleBroker("/topic", "/queue", "/user"); // Use both queue & topic
        registry.setApplicationDestinationPrefixes("/app"); // Prefix for messages sent to backend
        registry.setUserDestinationPrefix("/user"); // Enables per-user messaging
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the /ws-chat endpoint for WebSocket connections (as requested by user)
        // 1. With SockJS fallback (for browsers that don't fully support WebSockets)
        registry.addEndpoint("/ws-chat") // Changed to /ws-chat
                .setAllowedOrigins("*") // Allow connections from any origin (for development - refine in production)
                .withSockJS(); // Enable SockJS fallback

        // 2. Register the same /ws-chat endpoint without SockJS (for native WebSocket clients)
        registry.addEndpoint("/ws-chat") // Changed to /ws-chat
                .setAllowedOrigins("*"); // Allow connections from any origin (for development - refine in production)
    }
}

