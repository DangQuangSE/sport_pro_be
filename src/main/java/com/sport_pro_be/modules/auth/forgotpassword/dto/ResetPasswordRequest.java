package com.sport_pro_be.modules.auth.forgotpassword.dto;

import com.sport_pro_be.modules.auth.constant.AuthConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = AuthConstant.TOKEN_REQUIRED)
        String forgotPasswordToken,

        @NotBlank(message = AuthConstant.PASSWORD_REQUIRED)
        @Size(min = 6, message = AuthConstant.PASSWORD_MIN_SIZE)
        String newPassword
) {
}

