package com.sport_pro_be.modules.notification.service;

import com.sport_pro_be.modules.notification.config.DiscordNotificationProperties;
import com.sport_pro_be.modules.notification.repository.NotificationOutboxStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.Duration;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.LOG_CLEANED;
import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.LOG_CLEANUP_ERROR;
import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.LOG_QUEUE_STATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxCleanupService {

    private final NotificationOutboxStateStore stateStore;
    private final DiscordNotificationProperties properties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${app.notifications.discord.cleanup-delay}")
    public void cleanTerminalNotifications() {
        Instant now = clock.instant();
        int sent = cleanSent(now);
        int failed = cleanFailed(now);
        if (sent > 0 || failed > 0) {
            log.info(LOG_CLEANED, sent, failed);
        }
        observeQueueAge(now);
    }

    private int cleanSent(Instant now) {
        try {
            return stateStore.deleteSentBefore(
                    now.minus(properties.getSentRetention()), properties.getCleanupBatchSize());
        } catch (RuntimeException exception) {
            log.error(LOG_CLEANUP_ERROR, "SENT", exception.getClass().getSimpleName());
            return 0;
        }
    }

    private int cleanFailed(Instant now) {
        try {
            return stateStore.deleteFailedBefore(
                    now.minus(properties.getFailedRetention()), properties.getCleanupBatchSize());
        } catch (RuntimeException exception) {
            log.error(LOG_CLEANUP_ERROR, "FAILED", exception.getClass().getSimpleName());
            return 0;
        }
    }

    private void observeQueueAge(Instant now) {
        try {
            long pendingCount = stateStore.countPending();
            long ageSeconds = stateStore.findOldestActiveCreatedAt()
                    .map(oldest -> Math.max(0, Duration.between(oldest, now).toSeconds()))
                    .orElse(0L);
            log.info(LOG_QUEUE_STATE, pendingCount, ageSeconds);
        } catch (RuntimeException exception) {
            log.error(LOG_CLEANUP_ERROR, "QUEUE_STATE", exception.getClass().getSimpleName());
        }
    }
}
