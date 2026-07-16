package com.sport_pro_be.modules.notification.service;

import com.sport_pro_be.modules.notification.config.DiscordNotificationProperties;
import com.sport_pro_be.modules.notification.domain.NotificationOutbox;
import com.sport_pro_be.modules.notification.domain.OrderLineNotificationSnapshot;
import com.sport_pro_be.modules.notification.domain.OrderNotificationSnapshot;
import com.sport_pro_be.modules.notification.enums.NotificationStatus;
import com.sport_pro_be.modules.notification.enums.NotificationType;
import com.sport_pro_be.modules.notification.repository.NotificationOutboxRepository;
import com.sport_pro_be.modules.order.domain.Order;
import com.sport_pro_be.modules.order.domain.OrderItem;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

    private static final int SNAPSHOT_SCHEMA_VERSION = 1;
    private static final String NEW_ORDER_KEY_PREFIX = "NEW_ORDER:";

    private final NotificationOutboxRepository repository;
    private final DiscordNotificationProperties properties;

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueueNewOrder(Order order) {
        if (!properties.isEnabled()) {
            return;
        }

        String notificationKey = NEW_ORDER_KEY_PREFIX + order.getId();
        if (repository.findByNotificationKey(notificationKey).isPresent()) {
            return;
        }

        NotificationOutbox outbox = new NotificationOutbox();
        outbox.setNotificationKey(notificationKey);
        outbox.setNotificationType(NotificationType.NEW_ORDER);
        outbox.setStatus(NotificationStatus.PENDING);
        outbox.setOrder(order);
        outbox.setPayloadSnapshot(createSnapshot(order));
        outbox.setFormatVersion(SNAPSHOT_SCHEMA_VERSION);
        outbox.setNextAttemptAt(Instant.now());
        repository.save(outbox);
    }

    private OrderNotificationSnapshot createSnapshot(Order order) {
        List<OrderLineNotificationSnapshot> lines = order.getItems().stream()
                .sorted(Comparator.comparing(
                        OrderItem::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::createLineSnapshot)
                .toList();

        Instant createdAt = order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
        return new OrderNotificationSnapshot(
                SNAPSHOT_SCHEMA_VERSION,
                order.getId(),
                String.valueOf(order.getId()),
                createdAt,
                order.getTotalAmount(),
                properties.getCurrency(),
                lines);
    }

    private OrderLineNotificationSnapshot createLineSnapshot(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        String color = variant.getColor() == null
                ? variant.getColorOld()
                : variant.getColor().getName();

        return new OrderLineNotificationSnapshot(
                variant.getProduct().getName(),
                variant.getSku(),
                variant.getSize(),
                color,
                item.getQuantity());
    }
}
