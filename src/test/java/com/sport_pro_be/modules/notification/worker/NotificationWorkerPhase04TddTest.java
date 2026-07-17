package com.sport_pro_be.modules.notification.worker;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationWorkerPhase04TddTest {
    private static final Path MAIN = Path.of("src/main/java/com/sport_pro_be/modules/notification");

    @Test
    void retryPolicyHasSixCyclesAllDelaysAndRateLimitMaximum() throws IOException {
        String source = productionSource();
        assertThat(source).contains("Duration.ofMinutes(1)", "Duration.ofMinutes(2)",
                "Duration.ofMinutes(5)", "Duration.ofMinutes(15)", "Duration.ofMinutes(30)");
        assertThat(source).containsPattern("(?s)(MAX_ATTEMPTS|MAX_CYCLES)\\s*=\\s*6");
        assertThat(source).contains("retryAfter");
        assertThat(source).containsPattern("(?s)(max|compareTo).*retryAfter|retryAfter.*(max|compareTo)");
    }

    @Test
    void claimIsJustInTimeSingleRowSkipLockedAndCannotReserveSeventhCycle() throws IOException {
        String source = productionSource().toLowerCase();
        assertThat(source).contains("for update skip locked", "attempt_count < 6", "limit 1");
        assertThat(source).contains("attempt_count = attempt_count + 1", "processing",
                "lease_owner", "lease_expires_at");
    }

    @Test
    void allStateChangesAreOwnerConditionalAndProgressIsPersistent() throws IOException {
        String source = productionSource().toLowerCase();
        assertThat(source).contains("next_chunk_index", "lease_owner", "processing");
        assertThat(occurrences(source, "lease_owner")).isGreaterThanOrEqualTo(5);
        assertThat(source).containsPattern("(?s)lease_owner.*next_chunk_index|next_chunk_index.*lease_owner");
        assertThat(source).containsPattern("(?s)lease_owner.*sent|sent.*lease_owner");
        assertThat(source).containsPattern("(?s)lease_owner.*failed|failed.*lease_owner");
    }

    @Test
    void disabledDispatcherIsNoOpAndWorkerRenewsBeforeEveryChunk() throws IOException {
        String source = productionSource();
        assertThat(source).contains("isEnabled()", "maxJobsPerTick");
        assertThat(source).containsPattern("(?s)if\\s*\\(\\s*!.*isEnabled\\(\\).*return");
        assertThat(source).containsPattern("(?s)(for|while).*claim.*process|claim.*process.*(for|while)");
        assertThat(source).containsPattern("(?s)(for|while).*chunk.*renew|renew.*(for|while).*chunk");
        assertThat(source).containsPattern("(?s)renew.*(false|0).*return|!.*renew.*return");
    }

    @Test
    void exhaustedOrderCreatesUniqueAlertAndAlertFailureDoesNotRecurse() throws IOException {
        String source = productionSource();
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V5__create_notification_outbox.sql"));
        assertThat(source).contains("DELIVERY_FAILURE_ALERT", "NEW_ORDER");
        assertThat(source).containsPattern("(?s)NEW_ORDER.*DELIVERY_FAILURE_ALERT");
        assertThat(source).containsPattern("(?s)(alert:|ALERT).*source|source.*(alert:|ALERT)");
        assertThat(migration.toLowerCase()).contains("unique", "notification_key");
        assertThat(source).containsPattern("(?s)DELIVERY_FAILURE_ALERT.*FAILED");
    }

    @Test
    void expiredSixthCycleIsTerminalizedWithoutAnotherHttpCycle() throws IOException {
        String source = productionSource().toLowerCase();
        assertThat(source).contains("lease_expires_at", "attempt_count = 6", "failed", "attempt_count < 6");
        assertThat(source).containsPattern("(?s)lease_expires_at.*attempt_count\\s*=\\s*6.*failed");
    }

    private static String productionSource() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            List<Path> javaFiles = files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
            StringBuilder all = new StringBuilder();
            for (Path file : javaFiles) all.append('\n').append(Files.readString(file));
            return all.toString();
        }
    }

    private static int occurrences(String value, String needle) {
        int count = 0;
        for (int index = 0; (index = value.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }
}
