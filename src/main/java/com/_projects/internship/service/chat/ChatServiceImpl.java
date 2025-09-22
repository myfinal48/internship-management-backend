package com._projects.internship.service.chat;

import com._projects.internship.dto.chat.*;
import com._projects.internship.exception.ResourceNotFoundException;
import com._projects.internship.exception.UnauthorizedException;
import com._projects.internship.model.chat.Conversation;
import com._projects.internship.model.chat.Message;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.chat.ConversationRepository;
import com._projects.internship.repository.chat.MessageRepository;
import com._projects.internship.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main implementation of the chat service.
 * This service handles all chat-related operations in a modular way.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public MessageDTO sendMessage(User sender, SendMessageRequest request) {
        // Find recipient
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        // Check if users can chat
        if (!participantService.canUsersChat(sender, recipient)) {
            throw new UnauthorizedException("You are not authorized to chat with this user");
        }

        // Get or create conversation
        Conversation conversation = getOrCreateConversationEntity(sender, recipient);

        // Create message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .type(request.getType() != null ? 
                    Message.MessageType.valueOf(request.getType()) : 
                    Message.MessageType.TEXT)
                .build();

        // Handle reply
        if (request.getReplyToId() != null) {
            Message replyTo = messageRepository.findById(request.getReplyToId())
                    .orElse(null);
            if (replyTo != null && replyTo.getConversation().getId().equals(conversation.getId())) {
                message.setReplyTo(replyTo);
            }
        }

        // Set metadata if provided
        if (request.getMetadata() != null) {
            message.setMetadata(request.getMetadata());
        }

        // Save message
        message = messageRepository.save(message);

        // Update conversation last message info
        conversation.updateLastMessage(message);
        conversationRepository.save(conversation);

        // Convert to DTO
        MessageDTO messageDTO = MessageDTO.fromEntity(message, sender.getId());

        // Send via WebSocket to both users
        sendWebSocketMessage(sender.getId(), messageDTO);
        sendWebSocketMessage(recipient.getId(), messageDTO);

        log.info("Message sent from user {} to user {}", sender.getId(), recipient.getId());

        return messageDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> getConversationMessages(User currentUser, Long otherUserId, Pageable pageable) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if users can chat
        if (!participantService.canUsersChat(currentUser, otherUser)) {
            throw new UnauthorizedException("You are not authorized to chat with this user");
        }

        // Find conversation
        Optional<Conversation> conversation = conversationRepository.findDirectConversation(currentUser, otherUser);
        
        if (conversation.isEmpty()) {
            return Page.empty(pageable);
        }

        // Get messages
        Page<Message> messages = messageRepository.findByConversationForUser(
            conversation.get(), currentUser, pageable
        );

        // Mark messages as read
        markMessagesAsRead(conversation.get(), currentUser);

        // Convert to DTOs
        return messages.map(msg -> MessageDTO.fromEntity(msg, currentUser.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationDTO> getUserConversations(User user, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findByParticipant(user, pageable);
        
        return conversations.map(conv -> {
            // Count unread messages for this conversation
            int unreadCount = (int) messageRepository.countUnreadInConversation(conv, user);
            return ConversationDTO.fromEntity(conv, user.getId(), unreadCount);
        });
    }

    @Override
    public MessageDTO updateMessage(User user, Long messageId, UpdateMessageRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Check if user is the sender
        if (!message.getSender().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own messages");
        }

        // Check if message is not too old (e.g., 24 hours)
        if (message.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new UnauthorizedException("Message is too old to be edited");
        }

        // Edit the message
        message.editContent(request.getContent());
        
        if (request.getMetadata() != null) {
            message.setMetadata(request.getMetadata());
        }

        message = messageRepository.save(message);

        // Send update via WebSocket
        MessageDTO messageDTO = MessageDTO.fromEntity(message, user.getId());
        
        // Notify all participants
        message.getConversation().getParticipants().forEach(participant -> 
            sendWebSocketMessage(participant.getId(), messageDTO)
        );

        log.info("Message {} edited by user {}", messageId, user.getId());

        return messageDTO;
    }

    @Override
    public void deleteMessage(User user, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        // Check if user is participant in the conversation
        if (!message.getConversation().hasParticipant(user)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        // Soft delete for the user
        message.markAsDeletedBy(user);
        messageRepository.save(message);

        // Send deletion notification via WebSocket
        sendWebSocketDeletion(user.getId(), messageId);

        log.info("Message {} deleted for user {}", messageId, user.getId());
    }

    @Override
    public void markConversationAsRead(User reader, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Check if user is participant
        if (!conversation.hasParticipant(reader)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        markMessagesAsRead(conversation, reader);
        
        // Send read receipt via WebSocket
        sendWebSocketReadReceipt(reader.getId(), conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(User user) {
        return messageRepository.countTotalUnreadForUser(user);
    }

    @Override
    public ConversationDTO getOrCreateConversation(User user1, User user2) {
        // Check if users can chat
        if (!participantService.canUsersChat(user1, user2)) {
            throw new UnauthorizedException("These users are not authorized to chat");
        }

        Conversation conversation = getOrCreateConversationEntity(user1, user2);
        
        // Count unread for user1
        int unreadCount = (int) messageRepository.countUnreadInConversation(conversation, user1);
        
        return ConversationDTO.fromEntity(conversation, user1.getId(), unreadCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User user) {
        return participantService.getEligibleParticipants(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> searchMessages(User user, String query, Pageable pageable) {
        // This would require a more complex query, potentially using full-text search
        // For now, returning empty page
        log.warn("Message search not yet implemented");
        return Page.empty(pageable);
    }

    // Helper methods

    private Conversation getOrCreateConversationEntity(User user1, User user2) {
        // Try to find existing conversation
        Optional<Conversation> existing = conversationRepository.findDirectConversation(user1, user2);
        
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new conversation
        Conversation conversation = Conversation.builder()
                .type(Conversation.ConversationType.DIRECT)
                .isActive(true)
                .build();
        
        conversation.addParticipant(user1);
        conversation.addParticipant(user2);
        
        return conversationRepository.save(conversation);
    }

    private void markMessagesAsRead(Conversation conversation, User reader) {
        List<Message> unreadMessages = messageRepository.findUnreadInConversation(conversation, reader);
        
        unreadMessages.forEach(message -> {
            message.markAsReadBy(reader);
            messageRepository.save(message);
        });
    }

    private void sendWebSocketMessage(Long userId, MessageDTO message) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
            );
        } catch (Exception e) {
            log.error("Error sending WebSocket message to user {}: {}", userId, e.getMessage());
        }
    }

    private void sendWebSocketDeletion(Long userId, Long messageId) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/deletions",
                messageId
            );
        } catch (Exception e) {
            log.error("Error sending WebSocket deletion to user {}: {}", userId, e.getMessage());
        }
    }

    private void sendWebSocketReadReceipt(Long userId, Long conversationId) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/read-receipts",
                conversationId
            );
        } catch (Exception e) {
            log.error("Error sending WebSocket read receipt to user {}: {}", userId, e.getMessage());
        }
    }
}
