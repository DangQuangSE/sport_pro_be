package com.sport_pro_be.auth.interfaces;

public interface IEmailService {

    void sendOtpEmail(String recipient, String otpCode, long expiresInMinutes);
}
