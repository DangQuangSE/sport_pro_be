package com.sport_pro_be.modules.notification.domain;

import com.sport_pro_be.modules.notification.enums.NotificationFailureCategory;
import com.sport_pro_be.modules.notification.enums.NotificationStatus;
import com.sport_pro_be.modules.notification.enums.NotificationType;
import com.sport_pro_be.modules.order.domain.Order;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "notification_outbox")
@Getter
@Setter
@NoArgsConstructor
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_key", nullable = false, length = 160, unique = true)
    private String notificationKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_outbox_id")
    private NotificationOutbox sourceOutbox;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_snapshot", nullable = false, columnDefinition = "jsonb")
    @Valid
    private OrderNotificationSnapshot payloadSnapshot;

    @Column(name = "format_version", nullable = false)
    private int formatVersion = 1;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt = Instant.now();

    @Column(name = "lease_owner", length = 100)
    private String leaseOwner;

    @Column(name = "lease_expires_at")
    private Instant leaseExpiresAt;

    @Column(name = "next_chunk_index", nullable = false)
    private int nextChunkIndex;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_error_category", length = 60)
    private NotificationFailureCategory lastErrorCategory;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = now;
        nextAttemptAt = nextAttemptAt == null ? now : nextAttemptAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
