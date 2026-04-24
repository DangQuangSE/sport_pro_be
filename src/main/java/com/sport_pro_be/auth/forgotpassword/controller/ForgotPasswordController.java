package com.sport_pro_be.auth.forgotpassword.controller;

import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.sport_pro_be.auth.forgotpassword.dto.ForgotPasswordTokenResponse;
import com.sport_pro_be.auth.forgotpassword.dto.ResetPasswordRequest;
import com.sport_pro_be.auth.forgotpassword.dto.VerifyForgotPasswordOtpRequest;
import com.sport_pro_be.auth.forgotpassword.interfaces.IForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiMessageResponse> requestOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(forgotPasswordService.requestOtp(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ForgotPasswordTokenResponse> verifyOtp(@Valid @RequestBody VerifyForgotPasswordOtpRequest request) {
        return ResponseEntity.ok(forgotPasswordService.verifyOtp(request));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(forgotPasswordService.resetPassword(request));
    }
}
