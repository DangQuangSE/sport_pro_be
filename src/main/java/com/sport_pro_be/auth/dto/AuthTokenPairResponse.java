package com.sport_pro_be.auth.dto;

public record AuthTokenPairResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String email,
        String refreshToken
) {
}
