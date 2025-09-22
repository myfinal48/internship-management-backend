package com._projects.internship.service.chat;

import com._projects.internship.mapper.chat.ChatMessageDTO;
import com._projects.internship.model.chat.ChatMessage;
import com._projects.internship.model.chat.ChatMessageEntity;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.chat.ChatMessageRepository;
import com._projects.internship.repository.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessage message) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversation(Long user1Id, Long user2Id) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessagesForUser(Long userId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getUnreadMessagesForUser(Long userId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long recipientId, Long senderId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationSummaries(Long userId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional
    public void deleteAllMessagesForUser(Long userId) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }

    @Override
    @Transactional
    public void reactToMessage(Long messageId, Long userId, String reaction) {
        throw new UnsupportedOperationException("Chat feature is disabled");
    }
}