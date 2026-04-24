package com.sport_pro_be.auth.service;

import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.interfaces.IJwtService;
import com.sport_pro_be.config.AuthProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import static com.sport_pro_be.auth.constant.AuthConstant.APP_JWT_SECRET_INVALID;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService implements IJwtService {

    private final AuthProperties authProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(APP_JWT_SECRET_INVALID);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(authProperties.getJwtExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public long getExpirationSeconds() {
        return authProperties.getJwtExpirationMinutes() * 60;
    }

    @Override
    public String generateForgotPasswordToken(String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(authProperties.getForgotPasswordTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(email)
                .claim("type", "FORGOT_PASSWORD")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extractEmailFromForgotPasswordToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!"FORGOT_PASSWORD".equals(claims.get("type", String.class))) {
                return null;
            }
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
