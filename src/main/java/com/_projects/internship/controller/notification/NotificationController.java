package com._projects.internship.controller.notification;

import com._projects.internship.dto.notification.ArchiveAllUnreadRequestDTO;
import com._projects.internship.dto.notification.ArchiveNotificationRequestDTO;
import com._projects.internship.dto.notification.MarkReadRequestDTO;
import com._projects.internship.dto.notification.NotificationDTO;
import com._projects.internship.dto.notification.NotificationRequestDTO;
import com._projects.internship.model.notification.NotificationStatus;
import com._projects.internship.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/notifications")
@RequiredArgsConstructor
@Tag(name = "notification-controller", description = "User notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Create and send a notification",
            description = "Allows creating and sending a notification to specified recipients via configured channels. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "201", description = "Notification sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping
    public ResponseEntity<NotificationDTO> sendNotification(
            @Valid @RequestBody NotificationRequestDTO request) {
        NotificationDTO created = notificationService.sendNotification(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Retrieve notification by ID",
            description = "Allows retrieving complete details of a specific notification. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Notification found")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @Operation(
            summary = "Retrieve all notifications",
            description = "Allows retrieving all notifications in the system. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @Operation(
            summary = "Update a notification",
            description = "Allows updating notification details and propagating changes to recipients. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Notification updated successfully")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequestDTO request) {
        return ResponseEntity.ok(notificationService.updateNotification(id, request));
    }

    @Operation(
            summary = "Delete a notification",
            description = "Allows permanently deleting a notification from the system. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Notification deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get notifications by status",
            description = "Retrieve user's notifications filtered by status (UNREAD/READ/ARCHIVED)"
    )
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    @GetMapping("/status")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByStatus(
            @RequestParam Long userId,
            @RequestParam NotificationStatus status) {
        return ResponseEntity.ok(notificationService.getNotificationsByStatus(userId, status));
    }

    @Operation(
            summary = "Mark notifications as read",
            description = "Update read status for multiple notifications"
    )
    @ApiResponse(responseCode = "200", description = "Notifications marked as read")
    @PatchMapping("/mark-read")
    public ResponseEntity<Void> markNotificationsAsRead(
            @Valid @RequestBody MarkReadRequestDTO request) {
        notificationService.markAsRead(request.getNotificationIds(), request.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete user-specific notification",
            description = "Remove notification association for a specific user without deleting global notification"
    )
    @ApiResponse(responseCode = "204", description = "User notification removed successfully")
    @DeleteMapping("/{notificationId}/users/{userId}")
    public ResponseEntity<Void> deleteNotificationForUser(
            @PathVariable Long notificationId,
            @PathVariable Long userId) {
        notificationService.deleteNotificationForUser(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Archive notification",
            description = "Archive a notification for specific user while keeping it in the system"
    )
    @ApiResponse(responseCode = "200", description = "Notification archived successfully")
    @PatchMapping("/archive")
    public ResponseEntity<Void> archiveNotification(
            @Valid @RequestBody ArchiveNotificationRequestDTO request) {
        notificationService.archiveNotification(request.getNotificationId(), request.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get user notifications",
            description = "Retrieve notifications for specific user with optional unread filter"
    )
    @ApiResponse(responseCode = "200", description = "User notifications retrieved successfully")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean unreadOnly) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(
                        userId,
                        unreadOnly != null && unreadOnly
                )
        );
    }

    @Operation(
            summary = "Archive all unread notifications",
            description = "Archive all unread notifications for a specific user"
    )
    @ApiResponse(responseCode = "200", description = "All unread notifications archived successfully")
    @PatchMapping("/archive-all-unread")
    public ResponseEntity<Void> archiveAllUnreadNotifications(
            @Valid @RequestBody ArchiveAllUnreadRequestDTO request) {
        notificationService.archiveAllUnreadNotifications(request.getUserId());
        return ResponseEntity.ok().build();
    }
}