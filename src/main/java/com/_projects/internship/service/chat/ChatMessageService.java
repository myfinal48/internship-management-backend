package com._projects.internship.service.chat;

import com._projects.internship.mapper.chat.ChatMessageDTO;
import com._projects.internship.model.chat.ChatMessage;

import java.util.List;

public interface ChatMessageService {

    ChatMessageDTO saveMessage(ChatMessage message);

    List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id);

    List<ChatMessageDTO> getMessagesForUser(Long userId);

    List<ChatMessageDTO> getUnreadMessagesForUser(Long userId);

    void markMessagesAsRead(Long recipientId, Long senderId);

    long countUnreadMessages(Long userId);

    List<ChatMessageDTO> getConversationSummaries(Long userId);

    void deleteMessage(Long messageId, Long userId);

    void deleteAllMessagesForUser(Long userId);

    void reactToMessage(Long messageId, Long userId, String reaction);
}
