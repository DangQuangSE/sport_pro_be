package com.sport_pro_be.modules.auth.interfaces;

import com.sport_pro_be.modules.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.modules.auth.dto.LoginRequest;
import com.sport_pro_be.modules.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.modules.auth.dto.RegisterRequest;

public interface IAuthService {
    void requestRegistrationOtp(String email);
    void register(RegisterRequest request);
    AuthTokenPairResponse login(LoginRequest request);
    AuthTokenPairResponse refreshAccessToken(String refreshToken);
    void logout(String refreshToken);
    void verifyOtp(OtpVerifyRequest request);
    void resendOtp(String email);
}

