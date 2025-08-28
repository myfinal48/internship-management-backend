package com._projects.internship.controller.chat;

import com._projects.internship.dto.ErrorResponseDTO;
import com._projects.internship.mapper.chat.ChatMessageDTO;
import com._projects.internship.model.chat.ChatMessage;
import com._projects.internship.model.chat.ChatMessageEntity;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.service.UserService;
import com._projects.internship.service.chat.ChatMessageService;
import com._projects.internship.service.chat.ChatParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/chat")
@RequiredArgsConstructor
@Tag(name = "chat-controller", description = "Chat message management")
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationRepository applicationRepository;
    private final ChatParticipantService chatParticipantService;

    @PostMapping
    @Operation(
            summary = "Send a message",
            description = "Allows sending a message to another user. Students must have applied to the company's offer to write to them. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Message sent successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - student who has not applied")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        User recipient = userService.findByUsername(request.getRecipientName());

        if (currentUser.getRole() == Role.STUDENT && recipient.getRole() == Role.COMPANY) {
            boolean hasApplied = applicationRepository.existsByStudentIdAndOfferCompanyId(
                    currentUser.getId(), recipient.getId());

            if (!hasApplied) {
                return ResponseEntity
                        .status(403)
                        .body(ErrorResponseDTO.forbidden(
                                "You must first apply to an offer from this company before you can send them a message."));
            }
        }

        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(currentUser.getId())
                .senderName(currentUser.getUsername())
                .recipientId(recipient.getId())
                .type(ChatMessageEntity.MessageType.CHAT)
                .build();

        ChatMessageDTO savedMessage = chatMessageService.saveMessage(message);

        messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(),
                "/queue/messages",
                savedMessage);

        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/messages",
                savedMessage);

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping
    @Operation(
            summary = "Retrieve all my messages",
            description = "Allows retrieving all messages of the logged-in user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Messages retrieved")
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/unread")
    @Operation(
            summary = "Retrieve unread messages",
            description = "Allows retrieving all unread messages of the logged-in user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Unread messages retrieved")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getUnreadMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/conversation/{userId}")
    @Operation(
            summary = "Retrieve a conversation",
            description = "Allows retrieving the conversation history with another user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Conversation retrieved")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversation(currentUser.getId(), userId));
    }

    @PutMapping("/read/{senderId}")
    @Operation(
            summary = "Mark as read",
            description = "Allows marking all messages from a specific sender as read. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Messages marked as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.markMessagesAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    @Operation(
            summary = "Count unread messages",
            description = "Allows getting the number of unread messages of the logged-in user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Number of unread messages")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.countUnreadMessages(currentUser.getId()));
    }

    @GetMapping("/conversations")
    @Operation(
            summary = "Retrieve conversation summaries",
            description = "Allows getting the last message of each conversation of the user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Conversation summaries retrieved")
    public ResponseEntity<List<ChatMessageDTO>> getConversationSummaries(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversationSummaries(currentUser.getId()));
    }

    @GetMapping("/participants")
    @Operation(
            summary = "Retrieve available participants",
            description = "Allows getting the list of users (companies and students) with whom one can chat. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "List of available participants")
    public ResponseEntity<List<Map<String, Object>>> getParticipants(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Validate current user
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Delegate business logic to service layer for proper role-based filtering
            List<Map<String, Object>> participants = chatParticipantService.getAvailableParticipants(currentUser);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            // Log the exception and return empty list to prevent information leakage
            return ResponseEntity.ok(List.of());
        }
    }

    @DeleteMapping("/message/{messageId}")
    @Operation(
            summary = "Delete a message",
            description = "Allows deleting a specific message by its ID. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "204", description = "Message deleted")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(
            summary = "Delete all my messages",
            description = "Allows deleting all messages of the logged-in user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "204", description = "All messages deleted")
    public ResponseEntity<Void> deleteAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteAllMessagesForUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/message/{id}/react")
    @Operation(
            summary = "React to a message",
            description = "Allows adding an emoji reaction to a message. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Reaction added")
    public ResponseEntity<Void> reactToMessage(
            @PathVariable Long id,
            @RequestParam String reaction,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.reactToMessage(id, currentUser.getId(), reaction);
        return ResponseEntity.ok().build();
    }

    
    public static class SendMessageRequest {

        @NotBlank(message = "Message content is required")
        private String content;

        @NotBlank(message = "Recipient name is required")
        private String recipientName;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getRecipientName() {
            return recipientName;
        }

        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }
    }
}