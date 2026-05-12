package com.sport_pro_be.modules.auth.forgotpassword.dto;

public record ForgotPasswordTokenResponse(
        String message,
        String forgotPasswordToken
) {
}

