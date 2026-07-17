package com.sport_pro_be.modules.notification.repository;

import com.sport_pro_be.modules.notification.domain.NotificationOutbox;
import com.sport_pro_be.modules.notification.enums.NotificationFailureCategory;
import com.sport_pro_be.modules.notification.enums.NotificationStatus;
import com.sport_pro_be.modules.notification.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

@EnabledIfSystemProperty(named = "notification.postgres.it", matches = "true")
class NotificationOutboxPostgresIntegrationTest {

    private JdbcTemplate jdbc;
    private NotificationOutboxStateStore store;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                requiredProperty("notification.postgres.url"),
                requiredProperty("notification.postgres.username"),
                requiredProperty("notification.postgres.password"));
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS notification_outbox CASCADE");
        jdbc.execute("DROP TABLE IF EXISTS orders CASCADE");
        jdbc.execute("CREATE TABLE orders (id BIGINT PRIMARY KEY)");
        new ResourceDatabasePopulator(
                new ClassPathResource("db/migration/V5__create_notification_outbox.sql"))
                .execute(dataSource);
        jdbc.update("INSERT INTO orders(id) VALUES (1), (2), (3)");
        NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
        when(repository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return jdbc.query("""
                    SELECT id, notification_type, status, attempt_count, source_outbox_id
                    FROM notification_outbox WHERE id = ?
                    """, resultSet -> {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                NotificationOutbox outbox = new NotificationOutbox();
                outbox.setId(resultSet.getLong("id"));
                outbox.setNotificationType(NotificationType.valueOf(resultSet.getString("notification_type")));
                outbox.setStatus(NotificationStatus.valueOf(resultSet.getString("status")));
                outbox.setAttemptCount(resultSet.getInt("attempt_count"));
                outbox.setSourceOutboxId(resultSet.getObject("source_outbox_id", Long.class));
                return Optional.of(outbox);
            }, id);
        });
        store = new NotificationOutboxStateStore(jdbc, repository);
    }

    @Test
    void claimLeaseRetryAndOwnerConditionalCompletionExecuteOnPostgres() {
        Instant now = Instant.parse("2026-07-17T07:00:00Z");
        long id = insertNewOrder(1, "NEW_ORDER:1", "PENDING", now.minusSeconds(1));

        NotificationOutbox claimed = store.claimOne("worker-a", now, Duration.ofMinutes(2)).orElseThrow();
        assertThat(claimed.getId()).isEqualTo(id);
        assertThat(claimed.getAttemptCount()).isEqualTo(1);
        assertThat(store.markSent(id, "worker-b", 1, now.plusSeconds(1))).isFalse();
        assertThat(store.scheduleRetry(id, "worker-a", now.plusSeconds(10),
                NotificationFailureCategory.NETWORK, "network", now.plusSeconds(1))).isTrue();
        assertThat(store.claimOne("worker-b", now.plusSeconds(9), Duration.ofMinutes(2))).isEmpty();
        assertThat(store.claimOne("worker-b", now.plusSeconds(10), Duration.ofMinutes(2))).isPresent();
    }

    @Test
    void terminalFailureCreatesOneAlertAndKeepsSourceFailureCategoryAcrossAlertRetry() {
        Instant now = Instant.parse("2026-07-17T07:00:00Z");
        long sourceId = insertNewOrder(2, "NEW_ORDER:2", "PROCESSING", now);
        jdbc.update("UPDATE notification_outbox SET attempt_count=6, lease_owner='worker-a', lease_expires_at=? WHERE id=?",
                java.sql.Timestamp.from(now.plusSeconds(60)), sourceId);

        NotificationOutbox source = new NotificationOutbox();
        source.setId(sourceId);
        source.setNotificationType(NotificationType.NEW_ORDER);
        assertThat(store.markFailedAndCreateAlert(source, "worker-a",
                NotificationFailureCategory.AUTHENTICATION, "401", now)).isTrue();
        assertThat(store.markFailedAndCreateAlert(source, "worker-a",
                NotificationFailureCategory.PERMISSION, "403", now)).isFalse();

        Long alertId = jdbc.queryForObject(
                "SELECT id FROM notification_outbox WHERE notification_type='DELIVERY_FAILURE_ALERT'",
                Long.class);
        jdbc.update("UPDATE notification_outbox SET status='PROCESSING', attempt_count=1, lease_owner='alert-worker' WHERE id=?",
                alertId);
        assertThat(store.scheduleRetry(alertId, "alert-worker", now.plusSeconds(10),
                NotificationFailureCategory.NETWORK, "network", now)).isTrue();
        assertThat(store.findSourceFailureCategory(sourceId))
                .contains(NotificationFailureCategory.AUTHENTICATION);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM notification_outbox WHERE notification_type='DELIVERY_FAILURE_ALERT'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void cleanupIsBoundedAndDoesNotDeleteSourceWhileAlertReferencesIt() {
        Instant now = Instant.parse("2026-07-17T07:00:00Z");
        long referenced = insertNewOrder(1, "NEW_ORDER:1", "FAILED", now.minus(Duration.ofDays(31)));
        jdbc.update("UPDATE notification_outbox SET failed_at=? WHERE id=?",
                java.sql.Timestamp.from(now.minus(Duration.ofDays(31))), referenced);
        insertAlert(referenced, now.minus(Duration.ofDays(31)));
        long removable = insertNewOrder(3, "NEW_ORDER:3", "SENT", now.minus(Duration.ofDays(8)));
        jdbc.update("UPDATE notification_outbox SET sent_at=? WHERE id=?",
                java.sql.Timestamp.from(now.minus(Duration.ofDays(8))), removable);

        assertThat(store.deleteFailedBefore(now.minus(Duration.ofDays(30)), 10)).isEqualTo(1);
        assertThat(store.deleteSentBefore(now.minus(Duration.ofDays(7)), 1)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notification_outbox WHERE id=?",
                Integer.class, referenced)).isEqualTo(1);
    }

    private long insertNewOrder(long orderId, String key, String status, Instant timestamp) {
        return jdbc.queryForObject("""
                INSERT INTO notification_outbox (
                    notification_key, notification_type, status, order_id, payload_snapshot,
                    next_attempt_at, created_at, updated_at)
                VALUES (?, 'NEW_ORDER', ?, ?, '{}'::jsonb, ?, ?, ?)
                RETURNING id
                """, Long.class, key, status, orderId,
                java.sql.Timestamp.from(timestamp), java.sql.Timestamp.from(timestamp),
                java.sql.Timestamp.from(timestamp));
    }

    private void insertAlert(long sourceId, Instant timestamp) {
        jdbc.update("""
                INSERT INTO notification_outbox (
                    notification_key, notification_type, status, source_outbox_id, payload_snapshot,
                    next_attempt_at, created_at, updated_at, failed_at)
                VALUES (?, 'DELIVERY_FAILURE_ALERT', 'FAILED', ?, '{}'::jsonb, ?, ?, ?, ?)
                """, "ALERT:" + sourceId, sourceId, java.sql.Timestamp.from(timestamp),
                java.sql.Timestamp.from(timestamp), java.sql.Timestamp.from(timestamp),
                java.sql.Timestamp.from(timestamp));
    }

    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required integration-test property: " + name);
        }
        return value;
    }
}
