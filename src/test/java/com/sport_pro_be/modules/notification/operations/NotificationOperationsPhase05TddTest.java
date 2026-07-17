package com.sport_pro_be.modules.notification.operations;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationOperationsPhase05TddTest {
    private static final Path MAIN = Path.of("src/main/java/com/sport_pro_be/modules/notification");
    private static final Path ENV = Path.of(".env.example");
    private static final Path COMPOSE = Path.of("compose.yaml");
    private static final Path RUNBOOK = Path.of("docs/discord-order-notifications-runbook.md");

    @Test
    void cleanupUsesTerminalTimestampsRetentionWindowsAndBoundedBatch() throws IOException {
        String source = productionSource().toLowerCase();

        assertThat(source).contains("sent_at", "failed_at", "7", "30", "limit");
        assertThat(source).containsPattern("(?s)status\s*=\s*'sent'.*sent_at.*7");
        assertThat(source).containsPattern("(?s)status\s*=\s*'failed'.*failed_at.*30");
        assertThat(source).containsPattern("(?s)(limit|fetch first).*(:|\\?)?(batch|limit|max)");
    }

    @Test
    void cleanupCannotDeleteActiveRowsAndDoesNotDependOnDeliveryEnableFlag() throws IOException {
        String source = productionSource().toLowerCase();

        assertThat(source).contains("@scheduled", "clean");
        assertThat(source).doesNotContainPattern("(?s)(clean|retention).*isEnabled\\(\\).*(return|skip)");
        assertThat(source).containsPattern("(?s)delete.*status.*(sent|failed)");
        List<String> deleteStatements = Pattern.compile("(?s)\\\"\\\"\\\"(.*?)\\\"\\\"\\\"")
                .matcher(source)
                .results()
                .map(result -> result.group(1))
                .filter(sql -> sql.contains("delete from notification_outbox"))
                .toList();
        assertThat(deleteStatements).hasSize(2);
        assertThat(deleteStatements).allSatisfy(sql ->
                assertThat(sql).doesNotContain("status = 'pending'", "status = 'processing'"));
    }

    @Test
    void discordEnvironmentContractIsCompleteHasNoFallbackAndNoTokenSample() throws IOException {
        String env = Files.readString(ENV);
        String compose = Files.readString(COMPOSE);
        List<String> keys = List.of(
                "DISCORD_NOTIFICATIONS_ENABLED", "DISCORD_BOT_TOKEN", "DISCORD_ORDER_CHANNEL_ID",
                "DISCORD_ALERT_CHANNEL_ID", "DISCORD_ADMIN_ORDER_URL_TEMPLATE", "DISCORD_ORDER_CURRENCY",
                "DISCORD_CONNECT_TIMEOUT", "DISCORD_READ_TIMEOUT", "DISCORD_LEASE_DURATION",
                "DISCORD_MAX_JOBS_PER_TICK", "DISCORD_TIME_ZONE", "DISCORD_WORKER_DELAY",
                "DISCORD_SENT_RETENTION", "DISCORD_FAILED_RETENTION", "DISCORD_CLEANUP_BATCH_SIZE",
                "DISCORD_CLEANUP_DELAY");

        assertThat(env).contains(keys.toArray(String[]::new));
        assertThat(compose).contains(keys.toArray(String[]::new));
        for (String key : keys) {
            assertThat(compose).doesNotContainPattern(Pattern.quote("${" + key) + ":-");
        }
        assertThat(env).containsPattern("(?m)^DISCORD_BOT_TOKEN=\\s*$");
    }

    @Test
    void lifecycleObservabilityUsesStructuredPayloadFreeEvents() throws IOException {
        String source = productionSource();

        assertThat(source).contains(
                "notification_enqueued", "notification_claimed", "notification_sent",
                "notification_retry_scheduled", "notification_failed", "notification_cleaned");
        assertThat(source.toLowerCase()).doesNotContainPattern(
                "log\\.(info|warn|error|debug)\\([^;]*(payload|snapshot|customer|address|phone|bot.?token)");
    }

    @Test
    void runbookCoversLeastPrivilegeRotationInspectionFailureModesAndLimitations() throws IOException {
        assertThat(RUNBOOK).exists();
        String runbook = Files.readString(RUNBOOK).toLowerCase();

        assertThat(runbook).contains("view channel", "send messages", "rotate", "queue", "401", "403",
                "404", "429", "snapshot", "catalog", "rollback", "discord");
        assertThat(runbook).contains("alert").containsPattern("(?s)alert.*(cannot|may fail|limitation)");
    }

    @Test
    void manualRequeueIsSingleIdParameterizedGuardedAndTransactionSafe() throws IOException {
        assertThat(RUNBOOK).exists();
        String runbook = Files.readString(RUNBOOK).toLowerCase();

        assertThat(runbook).contains("begin", "for update", "where id =", "status = 'failed'", "commit", "rollback");
        assertThat(runbook).containsPattern("where\s+id\s*=\s*(:[a-z_]+|\\$1|\\?)");
        assertThat(runbook).contains("pending", "lease_owner", "lease_expires_at", "last_error",
                "attempt_count", "next_chunk_index");
        assertThat(runbook).contains("confirm", "backup");
        assertThat(runbook).doesNotContainPattern("(?s)update\s+notification_outbox\s+set(?:(?!where\s+id\s*=).)*;");
    }

    @Test
    void retentionConfigurationIsRequiredAndValidatedAtStartup() throws IOException {
        String properties = Files.readString(Path.of("src/main/resources/application.properties"));
        String source = productionSource();

        assertThat(properties).contains(
                "${DISCORD_SENT_RETENTION}", "${DISCORD_FAILED_RETENTION}",
                "${DISCORD_CLEANUP_BATCH_SIZE}", "${DISCORD_CLEANUP_DELAY}");
        assertThat(properties).doesNotContainPattern("\\$\\{DISCORD_(SENT|FAILED|CLEANUP)[^}]*:");
        assertThat(source).contains("sentRetention", "failedRetention", "cleanupBatchSize");
        assertThat(source).containsPattern("@(NotNull|NotBlank|Positive)");
    }

    private static String productionSource() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            StringBuilder all = new StringBuilder();
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
                all.append('\n').append(Files.readString(file));
            }
            return all.toString();
        }
    }
}
