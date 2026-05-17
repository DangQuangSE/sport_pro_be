package com.sport_pro_be.modules.auth.controller;

import com.sport_pro_be.modules.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.modules.auth.dto.LoginRequest;
import com.sport_pro_be.modules.auth.dto.LoginSuccessResponse;
import com.sport_pro_be.modules.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.modules.auth.dto.RegisterRequest;
import com.sport_pro_be.modules.auth.dto.ResendOtpRequest;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.interfaces.IAuthService;
import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.annotation.RateLimit;
import com.sport_pro_be.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.sport_pro_be.modules.auth.constant.AuthConstant.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final AuthProperties authProperties;

    @PostMapping("/register/request-otp")
    @RateLimit(requests = 3, periodInSeconds = 60)
    public ApiResponse<Void> requestRegistrationOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.requestRegistrationOtp(request.email());
        return ApiResponse.of(OTP_SENT_SUCCESS, null);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimit(requests = 5, periodInSeconds = 60)
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.of(REGISTRATION_SUCCESS, null);
    }

    @PostMapping("/login")
    @RateLimit(requests = 5, periodInSeconds = 60)
    public ApiResponse<LoginSuccessResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthTokenPairResponse tokenPair = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(tokenPair.refreshToken()).toString());
        return ApiResponse.of(LOGIN_SUCCESS, toLoginSuccessResponse(tokenPair));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<LoginSuccessResponse> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(request);
        AuthTokenPairResponse tokenPair = authService.refreshAccessToken(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(tokenPair.refreshToken()).toString());
        return ApiResponse.of(TOKEN_REFRESHED, toLoginSuccessResponse(tokenPair));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(resolveRefreshToken(request));
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshTokenCookie().toString());
        return ApiResponse.of(LOGOUT_SUCCESS, null);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request);
        return ApiResponse.of(OTP_VERIFIED_FOR_REGISTRATION, null);
    }

    @PostMapping("/resend-otp")
    @RateLimit(requests = 3, periodInSeconds = 60)
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request.email());
        return ApiResponse.of(OTP_RESENT_SUCCESS, null);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        User user = com.sport_pro_be.common.SecurityUtils.getCurrentUser();
        Map<String, Object> data = Map.of(
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "tier", user.getTier().name(),
                "totalSpending", user.getTotalSpending()
        );
        return ApiResponse.of(USER_DETAILS_RETRIEVED, data);
    }

    private LoginSuccessResponse toLoginSuccessResponse(AuthTokenPairResponse tokenPair) {
        return new LoginSuccessResponse(
                tokenPair.tokenType(),
                tokenPair.accessToken(),
                tokenPair.expiresInSeconds(),
                tokenPair.email()
        );
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (authProperties.getRefreshTokenCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(authProperties.getRefreshTokenCookieName(), refreshToken)
                .httpOnly(true)
                .secure(authProperties.isRefreshTokenCookieSecure())
                .sameSite(authProperties.getRefreshTokenCookieSameSite())
                .path(authProperties.getRefreshTokenCookiePath())
                .maxAge(authProperties.getRefreshTokenExpirationDays() * 24 * 60 * 60)
                .build();
    }

    private ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(authProperties.getRefreshTokenCookieName(), "")
                .httpOnly(true)
                .secure(authProperties.isRefreshTokenCookieSecure())
                .sameSite(authProperties.getRefreshTokenCookieSameSite())
                .path(authProperties.getRefreshTokenCookiePath())
                .maxAge(0)
                .build();
    }
}

