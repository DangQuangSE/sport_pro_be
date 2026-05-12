package com.sport_pro_be.modules.auth.forgotpassword.dto;

import com.sport_pro_be.modules.auth.constant.AuthConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = AuthConstant.EMAIL_REQUIRED)
        @Email(message = AuthConstant.EMAIL_INVALID)
        String email
) {
}

