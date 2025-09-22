package com._projects.internship.model.chat;

import com._projects.internship.model.security.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a chat message.
 * This entity is designed to be modular and reusable across different projects.
 */
@Entity
@Table(name = "chat_messages_v2", indexes = {
    @Index(name = "idx_message_conversation", columnList = "conversation_id"),
    @Index(name = "idx_message_sender", columnList = "sender_id"),
    @Index(name = "idx_message_created", columnList = "created_at"),
    @Index(name = "idx_message_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"conversation", "readBy", "deletedBy"})
@ToString(exclude = {"conversation", "readBy", "deletedBy"})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_edited")
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "edit_history", columnDefinition = "TEXT")
    private String editHistory; // JSON array of previous versions

    // Track who has read the message
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "message_read_receipts",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_read_message", columnList = "message_id"),
            @Index(name = "idx_read_user", columnList = "user_id")
        }
    )
    @Builder.Default
    private Set<User> readBy = new HashSet<>();

    // Soft delete - track who deleted the message for themselves
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "message_deletions",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = {
            @Index(name = "idx_deletion_message", columnList = "message_id"),
            @Index(name = "idx_deletion_user", columnList = "user_id")
        }
    )
    @Builder.Default
    private Set<User> deletedBy = new HashSet<>();

    // For attachments, reactions, etc. (JSON format for flexibility)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Reply to another message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM,
        NOTIFICATION
    }

    public enum MessageStatus {
        SENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    /**
     * Mark message as read by a user
     */
    public void markAsReadBy(User user) {
        if (readBy == null) {
            readBy = new HashSet<>();
        }
        readBy.add(user);
        if (status != MessageStatus.READ && !user.equals(sender)) {
            status = MessageStatus.READ;
        }
    }

    /**
     * Mark message as deleted for a user (soft delete)
     */
    public void markAsDeletedBy(User user) {
        if (deletedBy == null) {
            deletedBy = new HashSet<>();
        }
        deletedBy.add(user);
    }

    /**
     * Check if message is deleted for a specific user
     */
    public boolean isDeletedFor(User user) {
        return deletedBy != null && deletedBy.contains(user);
    }

    /**
     * Check if message is read by a specific user
     */
    public boolean isReadBy(User user) {
        return readBy != null && readBy.contains(user);
    }

    /**
     * Edit the message content
     */
    public void editContent(String newContent) {
        // Store previous content in edit history (you might want to use JSON here)
        if (this.editHistory == null) {
            this.editHistory = "[{\"content\":\"" + this.content + "\",\"editedAt\":\"" + LocalDateTime.now() + "\"}]";
        } else {
            // Append to existing history (simplified - in production use proper JSON handling)
            this.editHistory = this.editHistory.substring(0, this.editHistory.length() - 1) + 
                ",{\"content\":\"" + this.content + "\",\"editedAt\":\"" + LocalDateTime.now() + "\"}]";
        }
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }
}
