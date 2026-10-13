package com.transitease.repository;

import com.transitease.entity.Notification;
import com.transitease.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, Boolean isRead);

    long countByRecipientAndIsRead(User recipient, Boolean isRead);

    boolean existsByRecipientAndRelatedEntityTypeAndRelatedEntityIdAndEventType(
            User recipient, String relatedEntityType, Long relatedEntityId, String eventType);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
