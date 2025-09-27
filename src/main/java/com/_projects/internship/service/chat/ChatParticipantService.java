package com._projects.internship.service.chat;

import com._projects.internship.dto.chat.ConversationDTO;
import com._projects.internship.model.security.User;
import java.util.List;
import java.util.Map;


public interface ChatParticipantService {

    
    List<Map<String, Object>> getAvailableParticipants(User currentUser);
    
    
    boolean canUsersChat(User user1, User user2);
    
    List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User currentUser);
}