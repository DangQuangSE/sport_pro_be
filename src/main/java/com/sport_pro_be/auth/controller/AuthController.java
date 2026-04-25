package com.sport_pro_be.auth.controller;

import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.auth.dto.LoginRequest;
import com.sport_pro_be.auth.dto.LoginSuccessResponse;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.dto.ResendOtpRequest;
import com.sport_pro_be.auth.dto.RegisterRequest;
import com.sport_pro_be.auth.interfaces.IAuthService;
import com.sport_pro_be.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final AuthProperties authProperties;

    @PostMapping("/register/request-otp")
    public ApiMessageResponse requestRegistrationOtp(@Valid @RequestBody ResendOtpRequest request) {
        return authService.requestRegistrationOtp(request.email());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiMessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginSuccessResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthTokenPairResponse tokenPair = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(tokenPair.refreshToken()).toString());
        return toLoginSuccessResponse(tokenPair);
    }

    @PostMapping("/refresh-token")
    public LoginSuccessResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(request);
        AuthTokenPairResponse tokenPair = authService.refreshAccessToken(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(tokenPair.refreshToken()).toString());
        return toLoginSuccessResponse(tokenPair);
    }

    @PostMapping("/logout")
    public ApiMessageResponse logout(HttpServletRequest request, HttpServletResponse response) {
        ApiMessageResponse apiResponse = authService.logout(resolveRefreshToken(request));
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshTokenCookie().toString());
        return apiResponse;
    }

    @PostMapping("/verify-otp")
    public ApiMessageResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/resend-otp")
    public ApiMessageResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return authService.resendOtp(request.email());
    }

    @GetMapping("/me")
    public java.util.Map<String, Object> me(org.springframework.security.core.Authentication authentication) {
        com.sport_pro_be.auth.domain.User user = (com.sport_pro_be.auth.domain.User) authentication.getPrincipal();
        return java.util.Map.of(
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "authorities", authentication.getAuthorities()
        );
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
