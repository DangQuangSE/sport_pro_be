package com.sport_pro_be.modules.auth.dto;

import com.sport_pro_be.modules.auth.constant.AuthConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = AuthConstant.EMAIL_REQUIRED)
        @Email(message = AuthConstant.EMAIL_INVALID)
        String email,

        @NotBlank(message = AuthConstant.PASSWORD_REQUIRED)
        @Size(min = 6, message = AuthConstant.PASSWORD_MIN_SIZE)
        String password
) {
}

