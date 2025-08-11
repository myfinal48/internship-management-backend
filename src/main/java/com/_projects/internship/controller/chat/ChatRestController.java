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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@Tag(name = "chat-controller", description = "Gestion des messages de chat")
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationRepository applicationRepository;

    @PostMapping
    @Operation(
            summary = "Envoyer un message",
            description = "Permet d'envoyer un message à un autre utilisateur. Les étudiants doivent avoir postulé à une offre de l'entreprise pour pouvoir lui écrire. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Message envoyé avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - étudiant n'ayant pas postulé")
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
                                "Vous devez d'abord postuler à une offre de cette entreprise avant de pouvoir lui envoyer un message."));
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
            summary = "Récupérer tous mes messages",
            description = "Permet de récupérer tous les messages de l'utilisateur connecté. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Messages récupérés")
    public ResponseEntity<List<ChatMessageDTO>> getAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/unread")
    @Operation(
            summary = "Récupérer les messages non lus",
            description = "Permet de récupérer tous les messages non lus de l'utilisateur connecté. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Messages non lus récupérés")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getUnreadMessagesForUser(currentUser.getId()));
    }

    @GetMapping("/conversation/{userId}")
    @Operation(
            summary = "Récupérer une conversation",
            description = "Permet de récupérer l'historique de conversation avec un autre utilisateur. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Conversation récupérée")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversation(currentUser.getId(), userId));
    }

    @PutMapping("/read/{senderId}")
    @Operation(
            summary = "Marquer comme lu",
            description = "Permet de marquer tous les messages d'un expéditeur spécifique comme lus. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Messages marqués comme lus")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.markMessagesAsRead(currentUser.getId(), senderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    @Operation(
            summary = "Compter les messages non lus",
            description = "Permet d'obtenir le nombre de messages non lus de l'utilisateur connecté. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Nombre de messages non lus")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.countUnreadMessages(currentUser.getId()));
    }

    @GetMapping("/conversations")
    @Operation(
            summary = "Récupérer les résumés de conversations",
            description = "Permet d'obtenir le dernier message de chaque conversation de l'utilisateur. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Résumés des conversations récupérés")
    public ResponseEntity<List<ChatMessageDTO>> getConversationSummaries(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatMessageService.getConversationSummaries(currentUser.getId()));
    }

    @GetMapping("/participants")
    @Operation(
            summary = "Récupérer les participants disponibles",
            description = "Permet d'obtenir la liste des utilisateurs (entreprises et étudiants) avec qui on peut discuter. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Liste des participants disponibles")
    public ResponseEntity<List<Map<String, Object>>> getParticipants(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        List<Map<String, Object>> participants = Stream.concat(
                userService.getUsersByRole(Role.COMPANY).stream(),
                userService.getUsersByRole(Role.STUDENT).stream())
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(this::userResponseToParticipantMap)
                .toList();

        return ResponseEntity.ok(participants);
    }

    @DeleteMapping("/message/{messageId}")
    @Operation(
            summary = "Supprimer un message",
            description = "Permet de supprimer un message spécifique par son ID. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "204", description = "Message supprimé")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(
            summary = "Supprimer tous mes messages",
            description = "Permet de supprimer tous les messages de l'utilisateur connecté. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "204", description = "Tous les messages supprimés")
    public ResponseEntity<Void> deleteAllMessages(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        chatMessageService.deleteAllMessagesForUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/message/{id}/react")
    @Operation(
            summary = "Réagir à un message",
            description = "Permet d'ajouter une réaction emoji à un message. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Réaction ajoutée")
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
        participantInfo.put("fullName", user.getUsername());
        return participantInfo;
    }

    public static class SendMessageRequest {

        @NotBlank(message = "Le contenu du message est obligatoire")
        private String content;

        @NotBlank(message = "Le nom du destinataire est obligatoire")
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