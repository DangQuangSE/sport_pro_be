package com.sport_pro_be.auth.service;

import com.sport_pro_be.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;

    public void sendOtpEmail(String recipient, String otpCode, long expiresInMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getMailFrom());
        message.setTo(recipient);
        message.setSubject("[Sport Pro] Mã OTP đăng nhập của bạn");
        message.setText("Mã OTP của bạn là: " + otpCode + "\nHiệu lực trong " + expiresInMinutes + " phút.");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Không gửi được OTP qua email, vui lòng thử lại", ex);
        }
    }
}
