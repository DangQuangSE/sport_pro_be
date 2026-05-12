package com.sport_pro_be.modules.auth.dto;

import com.sport_pro_be.modules.auth.constant.AuthConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank(message = AuthConstant.EMAIL_REQUIRED)
        @Email(message = AuthConstant.EMAIL_INVALID)
        String email,

        @NotBlank(message = AuthConstant.OTP_CODE_REQUIRED)
        @Pattern(regexp = "^\\d{6}$", message = AuthConstant.OTP_INVALID_FORMAT)
        String otp
) {
}

