package com._projects.internship.repository.chat;

import com._projects.internship.model.chat.Conversation;
import com._projects.internship.model.chat.Message;
import com._projects.internship.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for chat messages.
 * Designed to be modular and reusable.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find messages in a conversation for a user (excluding deleted ones)
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationForUser(@Param("conversation") Conversation conversation, 
                                            @Param("user") User user, 
                                            Pageable pageable);

    /**
     * Find recent messages in a conversation for a user
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "ORDER BY m.createdAt DESC")
    List<Message> findRecentByConversation(@Param("conversation") Conversation conversation,
                                           @Param("user") User user,
                                           Pageable pageable);

    /**
     * Count unread messages in a conversation for a user
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy")
    long countUnreadInConversation(@Param("conversation") Conversation conversation, 
                                   @Param("user") User user);

    /**
     * Count total unread messages for a user across all conversations
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.conversation c " +
           "JOIN c.participants p " +
           "WHERE p = :user " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy")
    long countTotalUnreadForUser(@Param("user") User user);

    /**
     * Find unread messages in a conversation for a user
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND m.sender != :user " +
           "AND :user NOT MEMBER OF m.readBy " +
           "AND :user NOT MEMBER OF m.deletedBy " +
           "ORDER BY m.createdAt ASC")
    List<Message> findUnreadInConversation(@Param("conversation") Conversation conversation, 
                                           @Param("user") User user);

    /**
     * Mark messages as read in a conversation
     */
    @Modifying
    @Query("UPDATE Message m " +
           "SET m.status = 'READ' " +
           "WHERE m.conversation = :conversation " +
           "AND m.sender != :reader " +
           "AND :reader NOT MEMBER OF m.readBy")
    void markMessagesAsRead(@Param("conversation") Conversation conversation, 
                           @Param("reader") User reader);

    /**
     * Find message with full details
     */
    @Query("SELECT m FROM Message m " +
           "LEFT JOIN FETCH m.sender " +
           "LEFT JOIN FETCH m.conversation " +
           "LEFT JOIN FETCH m.readBy " +
           "WHERE m.id = :id")
    Optional<Message> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find messages by sender in a time range
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.sender = :sender " +
           "AND m.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY m.createdAt DESC")
    List<Message> findBySenderInTimeRange(@Param("sender") User sender,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * Delete (soft) messages older than a certain date for a user
     */
    @Modifying
    @Query(value = "INSERT INTO message_deletions (message_id, user_id) " +
           "SELECT m.id, :userId FROM chat_messages_v2 m " +
           "WHERE m.conversation_id IN (SELECT cp.conversation_id FROM conversation_participants cp WHERE cp.user_id = :userId) " +
           "AND m.created_at < :beforeDate " +
           "AND NOT EXISTS (SELECT 1 FROM message_deletions md WHERE md.message_id = m.id AND md.user_id = :userId)",
           nativeQuery = true)
    void softDeleteOldMessagesForUser(@Param("userId") Long userId,
                                      @Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Find last message in conversation
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation = :conversation " +
           "AND m.createdAt = (SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.conversation = :conversation)")
    Optional<Message> findLastMessageInConversation(@Param("conversation") Conversation conversation);
}
