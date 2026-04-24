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
    private long jwtExpirationMinutes = 60;

    @Min(1)
    private long otpExpirationMinutes = 5;

    @Min(0)
    private long otpResendCooldownSeconds = 60;

    @Min(1)
    private long forgotPasswordTokenExpirationMinutes = 15;

    @Min(1)
    private int forgotPasswordMaxAttempts = 5;

    @Min(1)
    private long refreshTokenExpirationDays = 14;

    @NotBlank
    private String refreshTokenCookieName = "refreshToken";

    private boolean refreshTokenCookieSecure;

    @NotBlank
    private String refreshTokenCookieSameSite = "Lax";

    @NotBlank
    private String refreshTokenCookiePath = "/api/auth";

    @NotBlank
    private String mailFrom;
}
