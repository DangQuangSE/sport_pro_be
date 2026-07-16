package com.sport_pro_be.modules.notification.migration;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationOutboxMigrationContractTest {

    private static final String MIGRATION = "db/migration/V5__create_notification_outbox.sql";

    @Test
    void v5DefinesDurableNonPiiOutboxStateAndOrderRelationship() throws Exception {
        String sql = migrationSql();

        assertThat(sql).containsIgnoringCase("create table notification_outbox");
        assertThat(sql).containsIgnoringCase("payload_snapshot jsonb");
        assertThat(sql).containsIgnoringCase("order_id");
        assertThat(sql).containsIgnoringCase("references orders");
        assertThat(sql).containsIgnoringCase("attempt_count");
        assertThat(sql).containsIgnoringCase("next_chunk_index");
        assertThat(sql).containsIgnoringCase("lease_owner");
        assertThat(sql).containsIgnoringCase("lease_expires_at");
        assertThat(sql).containsIgnoringCase("next_attempt_at");
        assertThat(sql).containsIgnoringCase("last_error_category");
    }

    @Test
    void v5EnforcesDeduplicationBoundsAndProvidesClaimAndRetentionIndexes() throws Exception {
        String sql = migrationSql();

        assertThat(sql).containsIgnoringCase("unique");
        assertThat(sql).containsIgnoringCase("notification_type");
        assertThat(sql).containsIgnoringCase("order_id");
        assertThat(sql).containsIgnoringCase("check");
        assertThat(sql).containsIgnoringCase("attempt_count");
        assertThat(sql).containsIgnoringCase("create index");
        assertThat(sql).containsIgnoringCase("status");
        assertThat(sql).containsIgnoringCase("next_attempt_at");
        assertThat(sql).containsIgnoringCase("sent_at");
        assertThat(sql).containsIgnoringCase("failed_at");
    }

    private static String migrationSql() throws Exception {
        ClassLoader loader = NotificationOutboxMigrationContractTest.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(MIGRATION)) {
            assertThat(stream).as("Phase 01 Flyway migration %s", MIGRATION).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
