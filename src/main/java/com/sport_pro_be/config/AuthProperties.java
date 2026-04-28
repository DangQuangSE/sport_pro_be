package com.sport_pro_be.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    @NotBlank
    private String jwtSecret;

    @Min(1)
    private long jwtExpirationMinutes;

    @Min(1)
    private long otpExpirationMinutes;

    @Min(0)
    private long otpResendCooldownSeconds;

    @Min(1)
    private long forgotPasswordTokenExpirationMinutes;

    @Min(1)
    private int forgotPasswordMaxAttempts;

    @Min(1)
    private long refreshTokenExpirationDays;

    @NotBlank
    private String refreshTokenCookieName;

    private boolean refreshTokenCookieSecure;

    @NotBlank
    private String refreshTokenCookieSameSite;

    @NotBlank
    private String refreshTokenCookiePath;

    @NotBlank
    private String mailFrom;
}
