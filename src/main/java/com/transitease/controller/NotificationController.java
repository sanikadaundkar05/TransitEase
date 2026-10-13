package com.transitease.controller;

import com.transitease.dto.NotificationResponse;
import com.transitease.dto.UnreadCountResponse;
import com.transitease.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false) Boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getMyNotifications(unreadOnly));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<NotificationResponse> markAsReadPatch(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
