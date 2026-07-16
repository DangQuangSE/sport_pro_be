package com.sport_pro_be.modules.notification.repository;

import com.sport_pro_be.modules.notification.domain.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {
    Optional<NotificationOutbox> findByNotificationKey(String notificationKey);
}
