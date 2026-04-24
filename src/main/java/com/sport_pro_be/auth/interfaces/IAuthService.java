package com.sport_pro_be.auth.interfaces;

import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.auth.dto.LoginRequest;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.dto.RegisterRequest;

public interface IAuthService {

    ApiMessageResponse requestRegistrationOtp(String email);

    ApiMessageResponse register(RegisterRequest request);

    AuthTokenPairResponse login(LoginRequest request);

    AuthTokenPairResponse refreshAccessToken(String refreshToken);

    ApiMessageResponse logout(String refreshToken);

    ApiMessageResponse verifyOtp(OtpVerifyRequest request);

    ApiMessageResponse resendOtp(String email);
}
