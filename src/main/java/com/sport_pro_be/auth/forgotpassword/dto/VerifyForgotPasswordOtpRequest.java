package com.sport_pro_be.auth.forgotpassword.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyForgotPasswordOtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "OTP code is required")
        String otpCode
) {
}
