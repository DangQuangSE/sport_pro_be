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
@ConfigurationProperties(prefix = "app.notifications.discord")
public class DiscordNotificationProperties {

    private boolean enabled;
    private String botToken;
    private String orderChannelId;
    private String alertChannelId;
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
    private ZoneId timeZone = ZoneId.of("Asia/Bangkok");

    @Min(1)
    @Max(100)
    private int maxJobsPerTick = 10;

    @AssertTrue(message = BOT_TOKEN_REQUIRED)
    public boolean isBotTokenConfigured() {
        return !enabled || hasText(botToken);
    }

    @AssertTrue(message = ORDER_CHANNEL_REQUIRED)
    public boolean isOrderChannelConfigured() {
        return !enabled || hasText(orderChannelId);
    }

    @AssertTrue(message = ALERT_CHANNEL_REQUIRED)
    public boolean isAlertChannelConfigured() {
        return !enabled || hasText(alertChannelId);
    }

    @AssertTrue(message = ADMIN_ORDER_URL_TEMPLATE_REQUIRED)
    public boolean isAdminOrderUrlTemplateConfigured() {
        return !enabled || isValidAdminOrderUrlTemplate();
    }

    @AssertTrue(message = HTTP_TIMEOUTS_INVALID)
    public boolean isLeaseLongerThanHttpTimeouts() {
        return leaseDuration != null && connectTimeout != null && readTimeout != null
                && !leaseDuration.isNegative() && !leaseDuration.isZero()
                && !connectTimeout.isNegative() && !connectTimeout.isZero()
                && !readTimeout.isNegative() && !readTimeout.isZero()
                && leaseDuration.compareTo(connectTimeout.plus(readTimeout)) > 0;
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
