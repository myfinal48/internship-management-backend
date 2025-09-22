package com._projects.internship.service.chat;

import com._projects.internship.dto.chat.*;
import com._projects.internship.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Main service interface for chat functionality.
 * This interface defines the contract for the chat module.
 */
public interface ChatService {

    /**
     * Send a new message
     */
    MessageDTO sendMessage(User sender, SendMessageRequest request);

    /**
     * Get messages for a conversation between two users
     */
    Page<MessageDTO> getConversationMessages(User currentUser, Long otherUserId, Pageable pageable);

    /**
     * Get all conversations for a user
     */
    Page<ConversationDTO> getUserConversations(User user, Pageable pageable);

    /**
     * Update/Edit a message
     */
    MessageDTO updateMessage(User user, Long messageId, UpdateMessageRequest request);

    /**
     * Delete a message (soft delete for the user)
     */
    void deleteMessage(User user, Long messageId);

    /**
     * Mark messages as read in a conversation
     */
    void markConversationAsRead(User reader, Long conversationId);

    /**
     * Get unread message count for a user
     */
    long getUnreadMessageCount(User user);

    /**
     * Get or create a conversation between two users
     */
    ConversationDTO getOrCreateConversation(User user1, User user2);

    /**
     * Get eligible participants for a user
     */
    List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User user);

    /**
     * Search messages in user's conversations
     */
    Page<MessageDTO> searchMessages(User user, String query, Pageable pageable);
}
