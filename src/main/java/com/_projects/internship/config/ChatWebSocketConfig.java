package com._projects.internship.config;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for the chat module.
 * This configuration is modular and can be easily integrated into any Spring Boot project.
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for broadcasting messages
        config.enableSimpleBroker(
            "/topic",        // For public/broadcast messages
            "/queue",        // For private messages
            "/user"          // For user-specific messages
        );
        
        // Set application destination prefix for messages FROM clients
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix
        config.setUserDestinationPrefix("/user");
        
        log.info("Chat WebSocket message broker configured");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")  // Configure based on your security needs
                .withSockJS();
        
        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*");
        
        log.info("Chat WebSocket STOMP endpoints registered at /ws-chat");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Authenticate STOMP CONNECT frames and set Principal
        registration.interceptors(webSocketAuthInterceptor);

        // Configure thread pool for incoming messages
        registration.taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(16)
                .queueCapacity(100);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Configure thread pool for outgoing messages
        registration.taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(16)
                .queueCapacity(100);
    }
}
