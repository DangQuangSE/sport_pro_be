package com.sport_pro_be.auth.controller;

import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.dto.LoginRequest;
import com.sport_pro_be.auth.dto.LoginSuccessResponse;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.dto.RegisterRequest;
import com.sport_pro_be.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiMessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ApiMessageResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-otp")
    public LoginSuccessResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return authService.verifyOtp(request);
    }
}
