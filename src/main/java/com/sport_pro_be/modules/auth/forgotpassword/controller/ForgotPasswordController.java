package com.sport_pro_be.modules.auth.forgotpassword.controller;

import com.sport_pro_be.modules.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ForgotPasswordTokenResponse;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ResetPasswordRequest;
import com.sport_pro_be.modules.auth.forgotpassword.dto.VerifyForgotPasswordOtpRequest;
import com.sport_pro_be.modules.auth.forgotpassword.interfaces.IForgotPasswordService;
import com.sport_pro_be.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final IForgotPasswordService forgotPasswordService;

    @PostMapping("/request-otp")
    public ApiResponse<Void> requestOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = forgotPasswordService.requestOtp(request);
        return ApiResponse.of(message, null);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<ForgotPasswordTokenResponse> verifyOtp(@Valid @RequestBody VerifyForgotPasswordOtpRequest request) {
        ForgotPasswordTokenResponse response = forgotPasswordService.verifyOtp(request);
        return ApiResponse.of(response.message(), response);
    }

    @PostMapping("/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = forgotPasswordService.resetPassword(request);
        return ApiResponse.of(message, null);
    }
}

