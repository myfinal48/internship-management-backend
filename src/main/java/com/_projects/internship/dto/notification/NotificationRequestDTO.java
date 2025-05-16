package com._projects.internship.dto.notification;

import com._projects.internship.model.core.Sector;
import com._projects.internship.model.notification.NotificationChannel;
import com._projects.internship.model.notification.NotificationType;
import com._projects.internship.model.security.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private NotificationType type;

    private NotificationChannel channel;

    private String subject;

    private Long senderId;

    private String content;

    private Role targetRole;

    private Sector sector;

    private Set<Long> userIds;
}