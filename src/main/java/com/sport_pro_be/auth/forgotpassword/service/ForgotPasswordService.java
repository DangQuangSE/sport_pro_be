package com.sport_pro_be.auth.forgotpassword.service;

import com.sport_pro_be.auth.domain.OtpType;
import com.sport_pro_be.auth.domain.OtpVerification;
import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.sport_pro_be.auth.forgotpassword.dto.ForgotPasswordTokenResponse;
import com.sport_pro_be.auth.forgotpassword.dto.ResetPasswordRequest;
import com.sport_pro_be.auth.forgotpassword.dto.VerifyForgotPasswordOtpRequest;
import com.sport_pro_be.auth.forgotpassword.interfaces.IForgotPasswordService;
import com.sport_pro_be.auth.interfaces.IEmailService;
import com.sport_pro_be.auth.interfaces.IJwtService;
import com.sport_pro_be.auth.repository.OtpVerificationRepository;
import com.sport_pro_be.auth.repository.RefreshTokenRepository;
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

import static com.sport_pro_be.auth.constant.AuthConstant.INVALID_OTP;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_EXPIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_INCORRECT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_LOCKED_TOO_MANY_ATTEMPTS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_REQUEST_TOO_FREQUENT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService implements IForgotPasswordService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final IJwtService jwtService;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public ApiMessageResponse requestOtp(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        
        // Neutral response, whether email exists or not
        String neutralResponse = "If your email exists in our system, an OTP has been sent.";

        if (!userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return new ApiMessageResponse(neutralResponse);
        }

        LocalDateTime now = LocalDateTime.now();

        // Check cooldown
        otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, OtpType.FORGOT_PASSWORD)
                .ifPresent(lastOtp -> {
                    LocalDateTime nextAllowed = lastOtp.getCreatedAt()
                            .plusSeconds(authProperties.getOtpResendCooldownSeconds());
                    if (nextAllowed.isAfter(now)) {
                        throw new ResponseStatusException(TOO_MANY_REQUESTS, OTP_REQUEST_TOO_FREQUENT);
                    }
                });

        // Invalidate old OTPs
        otpVerificationRepository.invalidateAllActiveByEmailAndType(normalizedEmail, OtpType.FORGOT_PASSWORD);

        // Create new OTP
        String otpCode = generateOtpCode();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(normalizedEmail);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setOtpType(OtpType.FORGOT_PASSWORD);
        otpVerification.setUsed(false);
        otpVerification.setAttemptCount(0);
        otpVerification.setOtpVerified(false);
        otpVerification.setExpiresAt(now.plusMinutes(authProperties.getOtpExpirationMinutes()));
        otpVerificationRepository.save(otpVerification);

        // Send email
        emailService.sendOtpEmail(normalizedEmail, otpCode, authProperties.getOtpExpirationMinutes());

        return new ApiMessageResponse(neutralResponse);
    }

    @Override
    @Transactional
    public ForgotPasswordTokenResponse verifyOtp(VerifyForgotPasswordOtpRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        
        OtpVerification otp = otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, OtpType.FORGOT_PASSWORD)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, INVALID_OTP));

        if (otp.isUsed()) {
            throw new ResponseStatusException(BAD_REQUEST, INVALID_OTP);
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setUsed(true);
            otpVerificationRepository.save(otp);
            throw new ResponseStatusException(BAD_REQUEST, OTP_EXPIRED);
        }

        if (!otp.getOtpCode().equals(request.otpCode())) {
            int currentAttemptCount = otp.getAttemptCount() == null ? 0 : otp.getAttemptCount();
            int newAttemptCount = currentAttemptCount + 1;
            otp.setAttemptCount(newAttemptCount);
            if (newAttemptCount >= authProperties.getForgotPasswordMaxAttempts()) {
                otp.setUsed(true);
                otpVerificationRepository.save(otp);
                throw new ResponseStatusException(BAD_REQUEST, OTP_LOCKED_TOO_MANY_ATTEMPTS);
            }
            otpVerificationRepository.save(otp);
            throw new ResponseStatusException(BAD_REQUEST, OTP_INCORRECT);
        }

        otp.setUsed(true);
        otp.setOtpVerified(true);
        otpVerificationRepository.save(otp);

        String token = jwtService.generateForgotPasswordToken(normalizedEmail);
        return new ForgotPasswordTokenResponse("OTP Verified. Use this token to reset your password.", token);
    }

    @Override
    @Transactional
    public ApiMessageResponse resetPassword(ResetPasswordRequest request) {
        String email = jwtService.extractEmailFromForgotPasswordToken(request.forgotPasswordToken());
        if (email == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid or expired forgot password token");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeActiveByUserId(user.getId(), LocalDateTime.now());

        return new ApiMessageResponse("Password has been reset successfully. Please login with your new password.");
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOtpCode() {
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
