package com.sport_pro_be.auth.service;

import com.sport_pro_be.auth.domain.EmailOtp;
import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.dto.LoginRequest;
import com.sport_pro_be.auth.dto.LoginSuccessResponse;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.dto.RegisterRequest;
import com.sport_pro_be.auth.repository.EmailOtpRepository;
import com.sport_pro_be.auth.repository.UserRepository;
import com.sport_pro_be.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthProperties authProperties;

    @Transactional
    public ApiMessageResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, "Email đã tồn tại");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);
        userRepository.save(user);

        return new ApiMessageResponse("Đăng ký thành công");
    }

    @Transactional
    public ApiMessageResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
        }

        LocalDateTime now = LocalDateTime.now();
        emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(normalizedEmail)
                .ifPresent(lastOtp -> {
                    LocalDateTime nextAllowed = lastOtp.getCreatedAt().plusSeconds(authProperties.getOtpResendCooldownSeconds());
                    if (nextAllowed.isAfter(now)) {
                        throw new ResponseStatusException(TOO_MANY_REQUESTS,
                                "Bạn đang yêu cầu OTP quá nhanh, vui lòng thử lại sau vài giây");
                    }
                });

        String otpCode = generateOtpCode();
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(normalizedEmail);
        emailOtp.setOtpCode(otpCode);
        emailOtp.setUsed(false);
        emailOtp.setExpiresAt(now.plusMinutes(authProperties.getOtpExpirationMinutes()));
        emailOtpRepository.save(emailOtp);

        emailService.sendOtpEmail(normalizedEmail, otpCode, authProperties.getOtpExpirationMinutes());
        return new ApiMessageResponse("OTP đã được gửi vào email của bạn");
    }

    @Transactional
    public LoginSuccessResponse verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        EmailOtp otp = emailOtpRepository
                .findTopByEmailIgnoreCaseAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, request.otp())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "OTP không hợp lệ"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "OTP đã hết hạn");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Không tìm thấy tài khoản"));

        otp.setUsed(true);
        emailOtpRepository.save(otp);

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        String token = jwtService.generateAccessToken(user);
        return new LoginSuccessResponse("Bearer", token, jwtService.getExpirationSeconds(), user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOtpCode() {
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
