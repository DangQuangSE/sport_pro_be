package com.sport_pro_be.modules.notification.service;

import com.sport_pro_be.modules.notification.client.DiscordClient;
import com.sport_pro_be.modules.notification.client.DiscordSendResult;
import com.sport_pro_be.modules.notification.client.DiscordSendStatus;
import com.sport_pro_be.modules.notification.config.DiscordNotificationProperties;
import com.sport_pro_be.modules.notification.domain.NotificationOutbox;
import com.sport_pro_be.modules.notification.dto.AllowedMentions;
import com.sport_pro_be.modules.notification.dto.DiscordMessageRequest;
import com.sport_pro_be.modules.notification.enums.NotificationFailureCategory;
import com.sport_pro_be.modules.notification.enums.NotificationType;
import com.sport_pro_be.modules.notification.repository.NotificationOutboxStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.*;
import static com.sport_pro_be.modules.notification.service.NotificationRetryPolicy.MAX_CYCLES;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationWorker {

    private final NotificationOutboxStateStore stateStore;
    private final DiscordOrderMessageFormatter formatter;
    private final DiscordClient discordClient;
    private final DiscordNotificationProperties properties;
    private final NotificationRetryPolicy retryPolicy;
    private final Clock clock;
    @Scheduled(fixedDelayString = "${app.notifications.discord.worker-delay}")
    public void dispatchDueNotifications() {
        if (!properties.isEnabled()) {
            return;
        }

        for (int processed = 0; processed < properties.getMaxJobsPerTick(); processed++) {
            Long outboxId = null;
            try {
                Instant now = clock.instant();
                if (stateStore.terminalizeOneExpiredSixthCycle(now)) {
                    continue;
                }

                String claimOwner = UUID.randomUUID().toString();
                Optional<NotificationOutbox> claimed = stateStore.claimOne(
                        claimOwner, now, properties.getLeaseDuration());
                if (claimed.isEmpty()) {
                    return;
                }
                outboxId = claimed.get().getId();
                process(claimed.get(), claimOwner);
            } catch (RuntimeException exception) {
                log.error(LOG_DISPATCH_ERROR, outboxId, exception.getClass().getSimpleName());
            }
        }
    }

    private void process(NotificationOutbox outbox, String claimOwner) {
        List<DiscordMessageRequest> chunks = outbox.getNotificationType() == NotificationType.NEW_ORDER
                ? formatter.format(outbox.getPayloadSnapshot())
                : List.of(formatFailureAlert(outbox));
        int startIndex = Math.min(outbox.getNextChunkIndex(), chunks.size());

        for (int chunkIndex = startIndex; chunkIndex < chunks.size(); chunkIndex++) {
            Instant beforeSend = clock.instant();
            if (!stateStore.renewLease(
                    outbox.getId(), claimOwner, beforeSend, properties.getLeaseDuration())) {
                log.warn(LOG_OWNERSHIP_LOST, outbox.getId(), "renew_lease");
                return;
            }

            String channelId = outbox.getNotificationType() == NotificationType.NEW_ORDER
                    ? properties.getOrderChannelId()
                    : properties.getAlertChannelId();
            DiscordSendResult result = discordClient.sendChannelMessage(channelId, chunks.get(chunkIndex));
            if (result.status() != DiscordSendStatus.SUCCESS) {
                handleFailure(outbox, claimOwner, result);
                return;
            }

            if (!stateStore.saveProgress(
                    outbox.getId(), claimOwner, chunkIndex + 1, chunks.size(), clock.instant())) {
                log.warn(LOG_OWNERSHIP_LOST, outbox.getId(), "save_progress");
                return;
            }
        }

        if (!stateStore.markSent(outbox.getId(), claimOwner, chunks.size(), clock.instant())) {
            log.warn(LOG_OWNERSHIP_LOST, outbox.getId(), "mark_sent");
        }
    }

    private void handleFailure(NotificationOutbox outbox, String claimOwner, DiscordSendResult result) {
        Instant now = clock.instant();
        NotificationFailureCategory category = failureCategory(result.status());
        String error = result.sanitizedError() == null ? DELIVERY_FAILED : result.sanitizedError();
        if (outbox.getAttemptCount() >= MAX_CYCLES) {
            boolean transitioned = stateStore.markFailedAndCreateAlert(
                    outbox, claimOwner, category, error, now);
            if (!transitioned) {
                log.warn(LOG_OWNERSHIP_LOST, outbox.getId(), "mark_failed");
            } else if (outbox.getNotificationType() == NotificationType.DELIVERY_FAILURE_ALERT) {
                log.error(LOG_ALERT_TERMINAL, outbox.getId(), category);
            } else {
                log.error(LOG_ORDER_TERMINAL, outbox.getId(), category);
            }
            return;
        }

        Duration delay = retryPolicy.nextDelay(outbox.getAttemptCount(), result.retryAfter());
        if (!stateStore.scheduleRetry(
                outbox.getId(), claimOwner, now.plus(delay), category, error, now)) {
            log.warn(LOG_OWNERSHIP_LOST, outbox.getId(), "schedule_retry");
        }
    }

    private DiscordMessageRequest formatFailureAlert(NotificationOutbox outbox) {
        String content = "**" + FAILURE_ALERT_TITLE + "**\n"
                + "Order: #" + outbox.getPayloadSnapshot().getOrderCode() + "\n"
                + "Source outbox: " + outbox.getSourceOutboxId() + "\n"
                + "Category: " + outbox.getLastErrorCategory();
        return new DiscordMessageRequest(content, AllowedMentions.none());
    }

    private NotificationFailureCategory failureCategory(DiscordSendStatus status) {
        return switch (status) {
            case RATE_LIMITED -> NotificationFailureCategory.RATE_LIMITED;
            case UNAUTHORIZED -> NotificationFailureCategory.AUTHENTICATION;
            case FORBIDDEN -> NotificationFailureCategory.PERMISSION;
            case NOT_FOUND -> NotificationFailureCategory.CHANNEL_NOT_FOUND;
            case CLIENT_ERROR -> NotificationFailureCategory.CLIENT_ERROR;
            case SERVER_ERROR -> NotificationFailureCategory.SERVER_ERROR;
            case NETWORK_ERROR -> NotificationFailureCategory.NETWORK;
            case INVALID_RESPONSE -> NotificationFailureCategory.INVALID_RESPONSE;
            case SUCCESS -> NotificationFailureCategory.UNKNOWN;
        };
    }
}
