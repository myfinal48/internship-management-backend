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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("${api.prefix}/chat")
@RequiredArgsConstructor
@Tag(name = "chat-controller", description = "Chat message operations")
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationRepository applicationRepository;

    @PostMapping
    @Operation(summary = "Send message", description = "Send a chat message to another user")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        
        // Find recipient by username
        User recipient = userService.findByUsername(request.getRecipientName());
        
        // Vérifier si l'étudiant a postulé à une offre de l'entreprise avant d'envoyer un message
        if (currentUser.getRole() == Role.STUDENT && recipient.getRole() == Role.COMPANY) {
            boolean hasApplied = applicationRepository.existsByStudentIdAndOfferCompanyId(
                currentUser.getId(), recipient.getId());
            
            if (!hasApplied) {
                return ResponseEntity
                    .status(403)
                    .body(ErrorResponseDTO.forbidden(
                        "Vous devez d'abord postuler à une offre de cette entreprise avant de pouvoir lui envoyer un message."
                    ));
            }
        }

        // Crée le message à partir des données validées
        ChatMessage message = ChatMessage.builder()
                .content(request.getContent())
                .senderId(currentUser.getId())
                .senderName(currentUser.getUsername())
                .recipientId(recipient.getId())
                .type(ChatMessageEntity.MessageType.CHAT)
                .build();

        // Sauvegarde du message
        ChatMessageDTO savedMessage = chatMessageService.saveMessage(message);

        // Envoi WebSocket au destinataire
        messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(),
                "/queue/messages",
                savedMessage
        );

        // Confirmation à l'expéditeur
        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/messages",
                savedMessage
        );

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping
    @Operation(summary = "Get all messages", description = "Get all messages for the current user")
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread messages", description = "Get all unread messages for the current user")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getUnreadMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/conversation/{userId}")
    @Operation(summary = "Get conversation", description = "Get conversation with another user")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversation(currentUser.getId(), userId));
    }

    @PutMapping("/read/{senderId}")
    @Operation(summary = "Mark as read", description = "Mark all messages from a specific sender as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.markMessagesAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread messages")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.countUnreadMessages(currentUser.getId()));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get conversation summaries", description = "Get latest message from each conversation")
    public ResponseEntity<List<ChatMessageDTO>> getConversationSummaries(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversationSummaries(currentUser.getId()));
    }

    @GetMapping("/participants")
    @Operation(summary = "Get available chat participants", description = "Returns users who can be messaged based on application status")
    public ResponseEntity<List<Map<String, Object>>> getParticipants(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<User> allowedUsers = new java.util.ArrayList<>();

        // Si l'utilisateur est un étudiant, il ne peut voir que les entreprises auxquelles il a postulé
        if (currentUser.getRole() == Role.STUDENT) {
            // Récupérer les IDs des entreprises auxquelles l'étudiant a postulé
            List<Long> companyIds = applicationRepository.findByStudentId(currentUser.getId())
                .stream()
                .map(app -> app.getInternshipOffer().getCompany().getId())
                .distinct()
                .toList();
            
            // Récupérer les entreprises correspondantes
            if (!companyIds.isEmpty()) {
                allowedUsers.addAll(userService.getUsersByRole(Role.COMPANY)
                    .stream()
                    .filter(company -> companyIds.contains(company.getId()))
                    .toList());
            }
        } 
        // Si l'utilisateur est une entreprise, elle ne peut voir que les étudiants qui ont postulé à ses offres
        else if (currentUser.getRole() == Role.COMPANY) {
            // Récupérer les IDs des étudiants qui ont postulé aux offres de l'entreprise
            List<Long> studentIds = applicationRepository.findByOfferCompanyId(currentUser.getId())
                .stream()
                .map(app -> app.getStudent().getId())
                .distinct()
                .toList();
            
            // Récupérer les étudiants correspondants
            if (!studentIds.isEmpty()) {
                allowedUsers.addAll(userService.getUsersByRole(Role.STUDENT)
                    .stream()
                    .filter(student -> studentIds.contains(student.getId()))
                    .toList());
            }
        } 
        // Pour les autres rôles (admin, teacher), montrer tous les utilisateurs
        else {
            allowedUsers.addAll(Stream.concat(
                userService.getUsersByRole(Role.COMPANY).stream(),
                userService.getUsersByRole(Role.STUDENT).stream()
            ).toList());
        }

        // Exclure l'utilisateur actuel et convertir en format de réponse
        List<Map<String, Object>> participants = allowedUsers.stream()
            .filter(user -> !user.getId().equals(currentUser.getId()))
            .map(this::userResponseToParticipantMap)
            .toList();

        return ResponseEntity.ok(participants);
    }


    @DeleteMapping("/message/{messageId}")
    @Operation(summary = "Delete message", description = "Delete a specific message by ID")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(summary = "Delete all messages", description = "Delete all messages for the current user")
    public ResponseEntity<Void> deleteAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteAllMessagesForUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/message/{id}/react")
    @Operation(summary = "React to message", description = "Add emoji reaction to a message")
    public ResponseEntity<Void> reactToMessage(
            @PathVariable Long id,
            @RequestParam String reaction,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.reactToMessage(id, currentUser.getId(), reaction);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> userResponseToParticipantMap(User user) {
        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("id", user.getId());
        participantInfo.put("fullName", user.getFirstName() + " " + user.getLastName());
        //participantInfo.put("username", user.getUsername());
        return participantInfo;
    }

    // Request DTO for sending messages
    public static class SendMessageRequest {

        @NotBlank(message = "Le contenu du message est obligatoire")
        private String content;

        @NotBlank(message = "Le nom du destinataire est obligatoire")
        private String recipientName;

        // Getters et Setters
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