package com.transitease.service;

import com.transitease.dto.NotificationResponse;
import com.transitease.entity.*;
import com.transitease.exception.ResourceNotFoundException;
import com.transitease.repository.NotificationRepository;
import com.transitease.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        }
        throw new RuntimeException("User not authenticated");
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Boolean unreadOnly) {
        User currentUser = getAuthenticatedUser();
        List<Notification> list;
        if (Boolean.TRUE.equals(unreadOnly)) {
            list = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false);
        } else {
            list = notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser);
        }
        return list.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = getAuthenticatedUser();
        return notificationRepository.countByRecipientAndIsRead(currentUser, false);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        User currentUser = getAuthenticatedUser();
        if (currentUser.getRole() == Role.PASSENGER) {
            if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to access this notification");
            }
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        User currentUser = getAuthenticatedUser();
        if (currentUser.getRole() == Role.PASSENGER) {
            if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to access this notification");
            }
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = getAuthenticatedUser();
        List<Notification> unreadList = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(currentUser, false);
        for (Notification n : unreadList) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unreadList);
    }

    @Transactional
    public Notification createApplicationStatusNotification(PassApplication application, ApplicationStatus status, String remarks) {
        if (application == null || application.getPassenger() == null || status == null) {
            return null;
        }

        User recipient = application.getPassenger();
        String eventType = "APPLICATION_" + status.name();
        String relatedEntityType = "APPLICATION";
        Long relatedEntityId = application.getId();

        // Avoid duplicate notifications for the same status event
        if (notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                recipient, relatedEntityType, relatedEntityId, eventType)) {
            return null;
        }

        String title;
        StringBuilder message = new StringBuilder();
        String categoryName = application.getPassCategory() != null ? application.getPassCategory().getName() : "Transit Pass";

        switch (status) {
            case VERIFIED -> {
                title = "Pass Application Verified";
                message.append("Your pass application #").append(application.getId())
                        .append(" (").append(categoryName).append(") has been verified by the officer.");
                if (remarks != null && !remarks.trim().isEmpty()) {
                    message.append(" Remarks: ").append(remarks.trim());
                }
            }
            case APPROVED -> {
                title = "Pass Application Approved";
                message.append("Congratulations! Your pass application #").append(application.getId())
                        .append(" (").append(categoryName).append(") has been approved.");
                if (remarks != null && !remarks.trim().isEmpty()) {
                    message.append(" Remarks: ").append(remarks.trim());
                }
            }
            case REJECTED -> {
                title = "Pass Application Rejected";
                message.append("Your pass application #").append(application.getId())
                        .append(" (").append(categoryName).append(") has been rejected.");
                if (remarks != null && !remarks.trim().isEmpty()) {
                    message.append(" Reason: ").append(remarks.trim());
                }
            }
            default -> {
                title = "Pass Application Update";
                message.append("Your pass application #").append(application.getId())
                        .append(" status has changed to ").append(status.name()).append(".");
                if (remarks != null && !remarks.trim().isEmpty()) {
                    message.append(" Remarks: ").append(remarks.trim());
                }
            }
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message.toString());
        notification.setType("APPLICATION_STATUS");
        notification.setEventType(eventType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createPassIssuedNotification(DigitalPass pass) {
        if (pass == null || pass.getPassenger() == null) {
            return null;
        }

        User recipient = pass.getPassenger();
        String eventType = "PASS_ISSUED";
        String relatedEntityType = "DIGITAL_PASS";
        Long relatedEntityId = pass.getId();

        // Avoid duplicate notifications for the same status event
        if (notificationRepository.existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
                recipient, relatedEntityType, relatedEntityId, eventType)) {
            return null;
        }

        String title = "Digital Pass Issued";
        String message = "Your digital pass (" + pass.getPassNumber() + ") has been successfully issued and is now active. " +
                "Valid until: " + pass.getValidityEndDate() + ".";

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("PASS_ISSUED");
        notification.setEventType(eventType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }
}
