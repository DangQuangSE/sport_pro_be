package com.sport_pro_be.modules.notification.client;

import java.time.Duration;

public record DiscordSendResult(
        DiscordSendStatus status,
        Duration retryAfter,
        String sanitizedError) {

    public static DiscordSendResult success() {
        return new DiscordSendResult(DiscordSendStatus.SUCCESS, null, null);
    }
}
