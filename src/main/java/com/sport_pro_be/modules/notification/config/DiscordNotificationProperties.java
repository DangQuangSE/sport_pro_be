package com.sport_pro_be.modules.notification.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.ZoneId;
import java.net.URI;
import java.net.URISyntaxException;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.*;

@Getter
@Setter
@Validated
@Component
@Lazy(false)
@ConfigurationProperties(prefix = "app.notifications.discord")
public class DiscordNotificationProperties {

    private boolean enabled;
    @NotBlank(message = BOT_TOKEN_REQUIRED)
    private String botToken;

    @NotBlank(message = ORDER_CHANNEL_REQUIRED)
    private String orderChannelId;

    @NotBlank(message = ALERT_CHANNEL_REQUIRED)
    private String alertChannelId;

    @NotBlank(message = ADMIN_ORDER_URL_TEMPLATE_REQUIRED)
    private String adminOrderUrlTemplate;
    @NotBlank(message = CURRENCY_REQUIRED)
    private String currency = "VND";

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(5);

    @NotNull
    private Duration readTimeout = Duration.ofSeconds(10);

    @NotNull
    private Duration leaseDuration = Duration.ofMinutes(2);

    @NotNull
    private Duration workerDelay = Duration.ofSeconds(10);

    @NotNull
    private Duration sentRetention = Duration.ofDays(7);

    @NotNull
    private Duration failedRetention = Duration.ofDays(30);

    @NotNull
    private Duration cleanupDelay = Duration.ofHours(1);

    @Min(1)
    @Max(10000)
    private int cleanupBatchSize = 500;

    @NotNull
    private ZoneId timeZone = ZoneId.of("Asia/Bangkok");

    @Min(1)
    @Max(100)
    private int maxJobsPerTick = 10;

    @AssertTrue(message = BOT_TOKEN_REQUIRED)
    public boolean isBotTokenConfigured() {
        return hasText(botToken);
    }

    @AssertTrue(message = ORDER_CHANNEL_REQUIRED)
    public boolean isOrderChannelConfigured() {
        return hasText(orderChannelId) && orderChannelId.chars().allMatch(Character::isDigit);
    }

    @AssertTrue(message = ALERT_CHANNEL_REQUIRED)
    public boolean isAlertChannelConfigured() {
        return hasText(alertChannelId) && alertChannelId.chars().allMatch(Character::isDigit);
    }

    @AssertTrue(message = ADMIN_ORDER_URL_TEMPLATE_REQUIRED)
    public boolean isAdminOrderUrlTemplateConfigured() {
        return isValidAdminOrderUrlTemplate();
    }

    @AssertTrue(message = HTTP_TIMEOUTS_INVALID)
    public boolean isLeaseLongerThanHttpTimeouts() {
        return leaseDuration != null && connectTimeout != null && readTimeout != null
                && !leaseDuration.isNegative() && !leaseDuration.isZero()
                && !connectTimeout.isNegative() && !connectTimeout.isZero()
                && !readTimeout.isNegative() && !readTimeout.isZero()
                && workerDelay != null && !workerDelay.isNegative() && !workerDelay.isZero()
                && leaseDuration.compareTo(connectTimeout.plus(readTimeout)) > 0;
    }

    @AssertTrue(message = RETENTION_INVALID)
    public boolean isRetentionValid() {
        return sentRetention != null && failedRetention != null
                && sentRetention.compareTo(Duration.ofDays(7)) >= 0
                && failedRetention.compareTo(Duration.ofDays(30)) >= 0;
    }

    @AssertTrue(message = CLEANUP_DELAY_INVALID)
    public boolean isCleanupDelayValid() {
        return cleanupDelay != null && !cleanupDelay.isNegative() && !cleanupDelay.isZero();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isValidAdminOrderUrlTemplate() {
        if (!hasText(adminOrderUrlTemplate)
                || !adminOrderUrlTemplate.contains("{orderId}")
                || adminOrderUrlTemplate.length() > 1000) {
            return false;
        }
        try {
            URI uri = new URI(adminOrderUrlTemplate.replace("{orderId}", "1"));
            return uri.isAbsolute()
                    && uri.getHost() != null
                    && ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()));
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
