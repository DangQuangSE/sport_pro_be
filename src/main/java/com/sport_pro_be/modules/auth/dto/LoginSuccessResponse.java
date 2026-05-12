package com.sport_pro_be.modules.auth.dto;

public record LoginSuccessResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        String email
) {
}

