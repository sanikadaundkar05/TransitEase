package com.transitease.dto;

import com.transitease.entity.Notification;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long recipientId;
    private String recipientEmail;
    private String recipientName;
    private String title;
    private String message;
    private String type;
    private String eventType;
    private Long relatedEntityId;
    private String relatedEntityType;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public static NotificationResponse fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationResponse resp = new NotificationResponse();
        resp.setId(notification.getId());
        if (notification.getRecipient() != null) {
            resp.setRecipientId(notification.getRecipient().getId());
            resp.setRecipientEmail(notification.getRecipient().getEmail());
            resp.setRecipientName(notification.getRecipient().getFullName());
        }
        resp.setTitle(notification.getTitle());
        resp.setMessage(notification.getMessage());
        resp.setType(notification.getType());
        resp.setEventType(notification.getEventType());
        resp.setRelatedEntityId(notification.getRelatedEntityId());
        resp.setRelatedEntityType(notification.getRelatedEntityType());
        resp.setIsRead(Boolean.TRUE.equals(notification.getIsRead()));
        resp.setCreatedAt(notification.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
