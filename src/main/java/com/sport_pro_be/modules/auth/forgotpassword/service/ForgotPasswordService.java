package com.sport_pro_be.modules.auth.forgotpassword.service;

import com.sport_pro_be.modules.auth.domain.OtpVerification;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.enums.OtpType;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ForgotPasswordTokenResponse;
import com.sport_pro_be.modules.auth.forgotpassword.dto.ResetPasswordRequest;
import com.sport_pro_be.modules.auth.forgotpassword.dto.VerifyForgotPasswordOtpRequest;
import com.sport_pro_be.modules.auth.forgotpassword.interfaces.IForgotPasswordService;
import com.sport_pro_be.modules.auth.interfaces.IEmailService;
import com.sport_pro_be.modules.auth.interfaces.IJwtService;
import com.sport_pro_be.modules.auth.repository.OtpVerificationRepository;
import com.sport_pro_be.modules.auth.repository.RefreshTokenRepository;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.config.AuthProperties;
import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

import static com.sport_pro_be.modules.auth.constant.AuthConstant.*;

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
    public String requestOtp(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (!userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return FORGOT_PASSWORD_OTP_SENT;
        }

        LocalDateTime now = LocalDateTime.now();

        // Check cooldown
        otpVerificationRepository
                .findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, OtpType.FORGOT_PASSWORD)
                .ifPresent(lastOtp -> {
                    LocalDateTime nextAllowed = lastOtp.getCreatedAt()
                            .plusSeconds(authProperties.getOtpResendCooldownSeconds());
                    if (nextAllowed.isAfter(now)) {
                        throw new TooManyRequestsException(OTP_REQUEST_TOO_FREQUENT);
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

        return FORGOT_PASSWORD_OTP_SENT;
    }

    @Override
    @Transactional
    public ForgotPasswordTokenResponse verifyOtp(VerifyForgotPasswordOtpRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        OtpVerification otp = otpVerificationRepository
                .findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, OtpType.FORGOT_PASSWORD)
                .orElseThrow(() -> new BadRequestException(INVALID_OTP));

        if (otp.isUsed()) {
            throw new BadRequestException(INVALID_OTP);
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setUsed(true);
            otpVerificationRepository.save(otp);
            throw new BadRequestException(OTP_EXPIRED);
        }

        if (!otp.getOtpCode().equals(request.otpCode())) {
            int currentAttemptCount = otp.getAttemptCount() == null ? 0 : otp.getAttemptCount();
            int newAttemptCount = currentAttemptCount + 1;
            otp.setAttemptCount(newAttemptCount);
            if (newAttemptCount >= authProperties.getForgotPasswordMaxAttempts()) {
                otp.setUsed(true);
                otpVerificationRepository.save(otp);
                throw new BadRequestException(OTP_LOCKED_TOO_MANY_ATTEMPTS);
            }
            otpVerificationRepository.save(otp);
            throw new BadRequestException(OTP_INCORRECT);
        }

        otp.setUsed(true);
        otp.setOtpVerified(true);
        otpVerificationRepository.save(otp);

        String token = jwtService.generateForgotPasswordToken(normalizedEmail);
        return new ForgotPasswordTokenResponse(OTP_VERIFIED_SUCCESS, token);
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        String email = jwtService.extractEmailFromForgotPasswordToken(request.forgotPasswordToken());
        if (email == null) {
            throw new BadRequestException(INVALID_FORGOT_PASSWORD_TOKEN);
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeActiveByUserId(user.getId(), LocalDateTime.now());

        return PASSWORD_RESET_SUCCESS;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOtpCode() {
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}

