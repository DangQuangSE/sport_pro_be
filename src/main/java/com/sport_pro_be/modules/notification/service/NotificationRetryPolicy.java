package com.sport_pro_be.modules.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.random.RandomGenerator;

@Component
@RequiredArgsConstructor
public class NotificationRetryPolicy {

    public static final int MAX_CYCLES = 6;
    private static final double MAX_JITTER_RATIO = 0.10;
    private static final List<Duration> RETRY_DELAYS = List.of(
            Duration.ofMinutes(1),
            Duration.ofMinutes(2),
            Duration.ofMinutes(5),
            Duration.ofMinutes(15),
            Duration.ofMinutes(30));

    private final RandomGenerator random;

    public Duration nextDelay(int reservedCycle, Duration retryAfter) {
        int delayIndex = Math.max(0, Math.min(reservedCycle - 1, RETRY_DELAYS.size() - 1));
        Duration policyDelay = RETRY_DELAYS.get(delayIndex);
        Duration baseDelay = retryAfter != null && retryAfter.compareTo(policyDelay) > 0
                ? retryAfter
                : policyDelay;
        long jitterBound = Math.max(1, Math.round(baseDelay.toMillis() * MAX_JITTER_RATIO));
        return baseDelay.plusMillis(random.nextLong(jitterBound));
    }
}
