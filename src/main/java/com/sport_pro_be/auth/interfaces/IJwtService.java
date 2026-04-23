package com.sport_pro_be.auth.interfaces;

import com.sport_pro_be.auth.domain.User;

public interface IJwtService {

    String generateAccessToken(User user);

    long getExpirationSeconds();
}
