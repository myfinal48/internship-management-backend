package com._projects.internship.service.chat;

import com._projects.internship.dto.chat.ConversationDTO;
import com._projects.internship.model.security.User;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing chat participants.
 * This interface defines the contract for participant management in the chat module.
 */
public interface ChatParticipantService {

    /**
     * Get available participants for a user based on business rules
     */
    List<Map<String, Object>> getAvailableParticipants(User currentUser);
    
    /**
     * Check if two users can chat based on business rules
     */
    boolean canUsersChat(User user1, User user2);
    
    /**
     * Get eligible participants as DTOs
     */
    List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User currentUser);
}