package com.sport_pro_be.auth.dto;

public record LoginSuccessResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String email
) {
}
