package com.sport_pro_be.modules.notification.repository;

import com.sport_pro_be.modules.notification.domain.NotificationOutbox;
import com.sport_pro_be.modules.notification.enums.NotificationFailureCategory;
import com.sport_pro_be.modules.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.FINAL_LEASE_EXPIRED;
import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.LOG_ALERT_TERMINAL;
import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.LOG_ORDER_TERMINAL;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxStateStore {

    private static final String CLAIM_SQL = """
            WITH candidate AS (
                SELECT id
                FROM notification_outbox
                WHERE attempt_count < 6
                  AND ((status = 'PENDING' AND next_attempt_at <= ?)
                    OR (status = 'PROCESSING' AND lease_expires_at <= ?))
                ORDER BY next_attempt_at, id
                FOR UPDATE SKIP LOCKED
                LIMIT 1
            )
            UPDATE notification_outbox outbox
            SET status = 'PROCESSING',
                attempt_count = attempt_count + 1,
                lease_owner = ?,
                lease_expires_at = ?,
                updated_at = ?
            FROM candidate
            WHERE outbox.id = candidate.id
            RETURNING outbox.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NotificationOutboxRepository repository;

    @Transactional
    public Optional<NotificationOutbox> claimOne(
            String leaseOwner,
            Instant now,
            Duration leaseDuration) {
        Timestamp timestamp = Timestamp.from(now);
        List<Long> claimedIds = jdbcTemplate.query(
                CLAIM_SQL,
                (resultSet, rowNumber) -> resultSet.getLong("id"),
                timestamp,
                timestamp,
                leaseOwner,
                Timestamp.from(now.plus(leaseDuration)),
                timestamp);
        return claimedIds.isEmpty() ? Optional.empty() : repository.findById(claimedIds.getFirst());
    }

    public boolean renewLease(Long id, String leaseOwner, Instant now, Duration leaseDuration) {
        return jdbcTemplate.update("""
                UPDATE notification_outbox
                SET lease_expires_at = ?, updated_at = ?
                WHERE id = ? AND status = 'PROCESSING' AND lease_owner = ?
                """,
                Timestamp.from(now.plus(leaseDuration)), Timestamp.from(now), id, leaseOwner) == 1;
    }

    public boolean saveProgress(Long id, String leaseOwner, int nextChunkIndex, int chunkCount, Instant now) {
        return jdbcTemplate.update("""
                UPDATE notification_outbox
                SET next_chunk_index = ?, chunk_count = ?, updated_at = ?
                WHERE id = ? AND status = 'PROCESSING' AND lease_owner = ?
                """,
                nextChunkIndex, chunkCount, Timestamp.from(now), id, leaseOwner) == 1;
    }

    public boolean markSent(Long id, String leaseOwner, int chunkCount, Instant now) {
        return jdbcTemplate.update("""
                UPDATE notification_outbox
                SET status = 'SENT', sent_at = ?, updated_at = ?,
                    next_chunk_index = ?, chunk_count = ?,
                    lease_owner = NULL, lease_expires_at = NULL,
                    last_error_category = NULL, last_error = NULL
                WHERE id = ? AND status = 'PROCESSING' AND lease_owner = ?
                """,
                Timestamp.from(now), Timestamp.from(now), chunkCount, chunkCount, id, leaseOwner) == 1;
    }

    public boolean scheduleRetry(
            Long id,
            String leaseOwner,
            Instant nextAttemptAt,
            NotificationFailureCategory category,
            String error,
            Instant now) {
        return jdbcTemplate.update("""
                UPDATE notification_outbox
                SET status = 'PENDING', next_attempt_at = ?, updated_at = ?,
                    lease_owner = NULL, lease_expires_at = NULL,
                    last_error_category = ?, last_error = ?
                WHERE id = ? AND status = 'PROCESSING' AND lease_owner = ?
                """,
                Timestamp.from(nextAttemptAt), Timestamp.from(now), category.name(), bounded(error), id, leaseOwner) == 1;
    }

    @Transactional
    public boolean markFailedAndCreateAlert(
            NotificationOutbox outbox,
            String leaseOwner,
            NotificationFailureCategory category,
            String error,
            Instant now) {
        int updated = jdbcTemplate.update("""
                UPDATE notification_outbox
                SET status = 'FAILED', failed_at = ?, updated_at = ?,
                    lease_owner = NULL, lease_expires_at = NULL,
                    last_error_category = ?, last_error = ?
                WHERE id = ? AND status = 'PROCESSING' AND lease_owner = ?
                """,
                Timestamp.from(now), Timestamp.from(now), category.name(), bounded(error),
                outbox.getId(), leaseOwner);
        if (updated == 1 && outbox.getNotificationType() == NotificationType.NEW_ORDER) {
            insertFailureAlert(outbox.getId(), category, now);
        }
        return updated == 1;
    }

    @Transactional
    public boolean terminalizeOneExpiredSixthCycle(Instant now) {
        List<Long> ids = jdbcTemplate.query("""
                SELECT id
                FROM notification_outbox
                WHERE status = 'PROCESSING'
                  AND lease_expires_at <= ?
                  AND attempt_count = 6
                ORDER BY lease_expires_at, id
                FOR UPDATE SKIP LOCKED
                LIMIT 1
                """,
                (resultSet, rowNumber) -> resultSet.getLong("id"),
                Timestamp.from(now));
        if (ids.isEmpty()) {
            return false;
        }

        NotificationOutbox outbox = repository.findById(ids.getFirst()).orElseThrow();
        int updated = jdbcTemplate.update("""
                UPDATE notification_outbox
                SET status = 'FAILED', failed_at = ?, updated_at = ?,
                    lease_owner = NULL, lease_expires_at = NULL,
                    last_error_category = 'TIMEOUT', last_error = ?
                WHERE id = ? AND status = 'PROCESSING'
                  AND lease_expires_at <= ? AND attempt_count = 6
                """,
                Timestamp.from(now), Timestamp.from(now), FINAL_LEASE_EXPIRED,
                outbox.getId(), Timestamp.from(now));
        if (updated == 1 && outbox.getNotificationType() == NotificationType.NEW_ORDER) {
            insertFailureAlert(outbox.getId(), NotificationFailureCategory.TIMEOUT, now);
            log.error(LOG_ORDER_TERMINAL, outbox.getId(), NotificationFailureCategory.TIMEOUT);
        } else if (updated == 1) {
            log.error(LOG_ALERT_TERMINAL, outbox.getId(), NotificationFailureCategory.TIMEOUT);
        }
        return updated == 1;
    }

    private void insertFailureAlert(Long sourceId, NotificationFailureCategory category, Instant now) {
        jdbcTemplate.update("""
                INSERT INTO notification_outbox (
                    notification_key, notification_type, status, order_id, source_outbox_id,
                    payload_snapshot, format_version, attempt_count, next_attempt_at,
                    next_chunk_index, last_error_category, created_at, updated_at)
                SELECT 'ALERT:' || source.id, 'DELIVERY_FAILURE_ALERT', 'PENDING', source.order_id, source.id,
                       source.payload_snapshot, source.format_version, 0, ?, 0, ?, ?, ?
                FROM notification_outbox source
                WHERE source.id = ? AND source.notification_type = 'NEW_ORDER'
                ON CONFLICT (notification_key) DO NOTHING
                """,
                Timestamp.from(now), category.name(), Timestamp.from(now), Timestamp.from(now), sourceId);
    }

    private static String bounded(String error) {
        if (error == null) {
            return null;
        }
        return error.substring(0, Math.min(error.length(), 500));
    }
}
