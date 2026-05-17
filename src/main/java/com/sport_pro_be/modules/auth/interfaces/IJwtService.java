package com.sport_pro_be.modules.auth.interfaces;

import com.sport_pro_be.modules.auth.domain.User;

public interface IJwtService {

    String generateAccessToken(User user);

    long getExpirationSeconds();

    String generateForgotPasswordToken(String email);

    String extractEmailFromForgotPasswordToken(String token);

    String extractEmailFromAccessToken(String token);

    java.util.List<String> extractRolesFromAccessToken(String token);

    Integer extractTokenVersionFromAccessToken(String token);
}

