package com.sport_pro_be.modules.notification.client;

import com.sport_pro_be.modules.notification.dto.AllowedMentions;
import com.sport_pro_be.modules.notification.dto.DiscordMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DiscordClientTddTest {

    private MockRestServiceServer server;
    private DiscordClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://discord.test/api/v10");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new DiscordClient(builder.build(), "secret-bot-token");
    }

    @Test
    void postsUsingBotAuthorizationAndExplicitlyDisablesMentions() {
        server.expect(once(), requestTo("https://discord.test/api/v10/channels/123/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bot secret-bot-token"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"content":"Order #42","allowed_mentions":{"parse":[]}}
                        """))
                .andRespond(withSuccess("{\"id\":\"987\"}", MediaType.APPLICATION_JSON));

        DiscordSendResult result = client.sendChannelMessage(
                "123", new DiscordMessageRequest("Order #42", new AllowedMentions(List.of())));

        assertThat(result.status()).isEqualTo(DiscordSendStatus.SUCCESS);
        assertThat(result.retryAfter()).isNull();
        server.verify();
    }

    @Test
    void classifiesRateLimitAndBoundsRetryAfter() {
        server.expect(requestTo("https://discord.test/api/v10/channels/123/messages"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"rate limited\",\"retry_after\":1.25}"));

        DiscordSendResult result = client.sendChannelMessage("123", request());

        assertThat(result.status()).isEqualTo(DiscordSendStatus.RATE_LIMITED);
        assertThat(result.retryAfter()).isEqualTo(Duration.ofMillis(1250));
        assertThat(result.sanitizedError()).doesNotContain("rate limited", "secret-bot-token");
    }

    @Test
    void classifiesPermanentAndTransientHttpFailuresWithoutLeakingBodies() {
        assertStatus(401, DiscordSendStatus.UNAUTHORIZED);
        assertStatus(403, DiscordSendStatus.FORBIDDEN);
        assertStatus(404, DiscordSendStatus.NOT_FOUND);
        assertStatus(500, DiscordSendStatus.SERVER_ERROR);
    }

    private void assertStatus(int status, DiscordSendStatus expected) {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://discord.test/api/v10");
        MockRestServiceServer localServer = MockRestServiceServer.bindTo(builder).build();
        DiscordClient localClient = new DiscordClient(builder.build(), "secret-bot-token");
        localServer.expect(requestTo("https://discord.test/api/v10/channels/123/messages"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.valueOf(status))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"sensitive upstream body\"}"));

        DiscordSendResult result = localClient.sendChannelMessage("123", request());

        assertThat(result.status()).isEqualTo(expected);
        assertThat(result.sanitizedError()).doesNotContain("sensitive upstream body", "secret-bot-token");
        localServer.verify();
    }

    private static DiscordMessageRequest request() {
        return new DiscordMessageRequest("Order #42", new AllowedMentions(List.of()));
    }
}
