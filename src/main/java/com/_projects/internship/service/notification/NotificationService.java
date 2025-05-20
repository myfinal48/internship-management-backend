package com._projects.internship.service.notification;


import com._projects.internship.dto.notification.NotificationDTO;
import com._projects.internship.dto.notification.NotificationRequestDTO;
import com._projects.internship.model.notification.NotificationStatus;

import java.util.List;

public interface NotificationService {
    NotificationDTO getNotificationById(Long notificationId);
    List<NotificationDTO> getAllNotifications();

    void deleteNotification(Long notificationId);
    NotificationDTO sendNotification(NotificationRequestDTO request);
    void markAsRead(List<Long> notificationIds, Long userId);
    void deleteNotificationForUser(Long notificationId, Long userId);
    NotificationDTO updateNotification(Long notificationId, NotificationRequestDTO request);
    void archiveNotification(Long notificationId, Long userId);
    List<NotificationDTO> getUserNotifications(Long userId, boolean unreadOnly);
    List<NotificationDTO> getNotificationsByStatus(Long userId, NotificationStatus status);
}