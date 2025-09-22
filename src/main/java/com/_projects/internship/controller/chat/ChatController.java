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

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.public")
    @SendTo("/topic/public")
    public ChatMessage handlePublicMessage(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        return ChatMessage.builder()
                .type(ChatMessageEntity.MessageType.LEAVE)
                .senderName(principal != null ? principal.getName() : "system")
                .content("Chat feature is disabled")
                .build();
    }

    @MessageMapping("/chat.private")
    public void handlePrivateMessage(
            @Payload ChatMessage chatMessage,
            Principal principal) {
        // Chat disabled: no-op
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/public")
    public ChatMessage handleUserJoin(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        return ChatMessage.builder()
                .type(ChatMessageEntity.MessageType.LEAVE)
                .senderName(principal != null ? principal.getName() : "system")
                .content("Chat feature is disabled")
                .build();
    }

    @MessageMapping("/chat.leave")
    @SendTo("/topic/public")
    public ChatMessage handleUserLeave(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        return ChatMessage.builder()
                .type(ChatMessageEntity.MessageType.LEAVE)
                .senderName(principal != null ? principal.getName() : "system")
                .content("Chat feature is disabled")
                .build();
    }
}