package com.sport_pro_be.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "OTP phải có đúng 6 chữ số") String otp
) {
}
