package com.sport_pro_be.modules.auth.forgotpassword.dto;

import com.sport_pro_be.modules.auth.constant.AuthConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyForgotPasswordOtpRequest(
        @NotBlank(message = AuthConstant.EMAIL_REQUIRED)
        @Email(message = AuthConstant.EMAIL_INVALID)
        String email,

        @NotBlank(message = AuthConstant.OTP_CODE_REQUIRED)
        String otpCode
) {
}

