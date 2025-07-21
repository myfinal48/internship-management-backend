package com._projects.internship.controller.chat;

import com._projects.internship.model.chat.ChatMessage;
import com._projects.internship.model.chat.ChatMessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles messages sent to /app/chat.public
     * Broadcasts the message to all subscribers of /topic/public
     */
    @MessageMapping("/chat.public")
    @SendTo("/topic/public")
    public ChatMessage handlePublicMessage(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        // Set the sender if not already set (use the principal's name)
        if (chatMessage.getSenderName() == null) {
            chatMessage.setSenderName(principal.getName());
        }

        return chatMessage;
    }

    /**
     * Handles messages sent to /app/chat.private
     * Sends the message to the specific user's private queue
     */
    @MessageMapping("/chat.private")
    public void handlePrivateMessage(
            @Payload ChatMessage chatMessage,
            Principal principal
    ) {
        // Set the sender if not already set (use the principal's name)
        if (chatMessage.getSenderName() == null) {
            chatMessage.setSenderName(principal.getName());
        }

        // Validate that recipient is specified for private messages
        if (chatMessage.getRecipientName() == null || chatMessage.getRecipientName().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient is required for private messages");
        }

        // Send it to the recipient's private queue
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientName(),
                "/queue/private",
                chatMessage
        );

        // Also send a copy to the sender's queue (so they can see their sent messages)
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/private",
                chatMessage
        );
    }

    /**
     * Handles user join events
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/public")
    public ChatMessage handleUserJoin(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        // Add username to the web socket session
        headerAccessor.getSessionAttributes().put("username", principal.getName());

        return ChatMessage.builder()
                .type(ChatMessageEntity.MessageType.JOIN)
                .senderName(principal.getName())
                .content(principal.getName() + " joined the chat")
                .build();
    }

    /**
     * Handles user leave events
     */
    @MessageMapping("/chat.leave")
    @SendTo("/topic/public")
    public ChatMessage handleUserLeave(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal
    ) {
        return ChatMessage.builder()
                .type(ChatMessageEntity.MessageType.LEAVE)
                .senderName(principal.getName())
                .content(principal.getName() + " left the chat")
                .build();
    }
}