package com.sport_pro_be.modules.notification.config;

import com.sport_pro_be.modules.notification.client.DiscordClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class DiscordRestClientConfig {

    private static final String DISCORD_API_BASE_URL = "https://discord.com/api/v10";

    @Bean
    RestClient discordRestClient(DiscordNotificationProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(DISCORD_API_BASE_URL)
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    DiscordClient discordClient(RestClient discordRestClient, DiscordNotificationProperties properties) {
        return new DiscordClient(discordRestClient, properties.getBotToken());
    }
}
