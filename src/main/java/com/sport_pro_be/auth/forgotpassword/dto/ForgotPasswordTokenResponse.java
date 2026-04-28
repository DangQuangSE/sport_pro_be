package com.sport_pro_be.auth.forgotpassword.dto;

public record ForgotPasswordTokenResponse(
        String message,
        String forgotPasswordToken
) {
}
