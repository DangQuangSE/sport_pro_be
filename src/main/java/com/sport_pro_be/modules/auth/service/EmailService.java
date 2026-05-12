package com.sport_pro_be.modules.auth.service;

import com.sport_pro_be.modules.auth.interfaces.IEmailService;
import com.sport_pro_be.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;

    @Override
    @Async("taskExecutor")
    public void sendOtpEmail(String recipient, String otpCode, long expiresInMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(authProperties.getMailFrom());
        message.setTo(recipient);
        message.setSubject("[Sport Pro] Your login OTP code");
        message.setText("Your OTP code is: " + otpCode + "\nIt will expire in " + expiresInMinutes + " minutes.");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Unable to send OTP email. Please try again", ex);
        }
    }
}

