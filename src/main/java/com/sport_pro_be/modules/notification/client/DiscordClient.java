package com.sport_pro_be.modules.notification.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport_pro_be.modules.notification.dto.DiscordMessageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.*;

public class DiscordClient {

    private static final int MAX_ERROR_BODY_BYTES = 8192;
    private static final Duration MAX_RETRY_AFTER = Duration.ofHours(24);

    private final RestClient restClient;
    private final String botAuthorization;
    private final ObjectMapper objectMapper;

    public DiscordClient(RestClient restClient, String botToken) {
        this(restClient, botToken, new ObjectMapper());
    }

    DiscordClient(RestClient restClient, String botToken, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.botAuthorization = "Bot " + botToken;
        this.objectMapper = objectMapper;
    }

    public DiscordSendResult sendChannelMessage(String channelId, DiscordMessageRequest message) {
        try {
            return restClient.post()
                    .uri("/channels/{channelId}/messages", channelId)
                    .header(HttpHeaders.AUTHORIZATION, botAuthorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(message)
                    .exchange((request, response) -> classify(
                            response.getStatusCode().value(),
                            response.getBody().readNBytes(MAX_ERROR_BODY_BYTES + 1)));
        } catch (RestClientException exception) {
            return new DiscordSendResult(DiscordSendStatus.NETWORK_ERROR, null, DISCORD_NETWORK_ERROR);
        }
    }

    private DiscordSendResult classify(int status, byte[] responseBody) {
        if (status >= 200 && status < 300) {
            return DiscordSendResult.success();
        }
        if (status == 429) {
            Optional<Duration> retryAfter = parseRetryAfter(responseBody);
            return retryAfter
                    .map(duration -> new DiscordSendResult(
                            DiscordSendStatus.RATE_LIMITED, duration, DISCORD_RATE_LIMITED))
                    .orElseGet(() -> failure(DiscordSendStatus.INVALID_RESPONSE, DISCORD_INVALID_RESPONSE));
        }
        if (status == 401) {
            return failure(DiscordSendStatus.UNAUTHORIZED, DISCORD_UNAUTHORIZED);
        }
        if (status == 403) {
            return failure(DiscordSendStatus.FORBIDDEN, DISCORD_FORBIDDEN);
        }
        if (status == 404) {
            return failure(DiscordSendStatus.NOT_FOUND, DISCORD_CHANNEL_NOT_FOUND);
        }
        if (status >= 500) {
            return failure(DiscordSendStatus.SERVER_ERROR, DISCORD_SERVER_ERROR);
        }
        return failure(DiscordSendStatus.CLIENT_ERROR, DISCORD_CLIENT_ERROR);
    }

    private Optional<Duration> parseRetryAfter(byte[] responseBody) {
        if (responseBody.length > MAX_ERROR_BODY_BYTES) {
            return Optional.empty();
        }
        try {
            JsonNode retryAfter = objectMapper.readTree(responseBody).path("retry_after");
            double seconds = retryAfter.doubleValue();
            if (!retryAfter.isNumber() || !Double.isFinite(seconds) || seconds <= 0) {
                return Optional.empty();
            }
            Duration parsed = Duration.ofMillis(Math.round(seconds * 1000));
            return Optional.of(parsed.compareTo(MAX_RETRY_AFTER) > 0 ? MAX_RETRY_AFTER : parsed);
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private static DiscordSendResult failure(DiscordSendStatus status, String message) {
        return new DiscordSendResult(status, null, message);
    }
}
