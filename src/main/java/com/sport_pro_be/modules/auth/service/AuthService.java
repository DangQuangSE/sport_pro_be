package com.sport_pro_be.modules.auth.service;

import com.sport_pro_be.modules.auth.domain.OtpVerification;
import com.sport_pro_be.modules.auth.domain.RefreshToken;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.modules.auth.dto.LoginRequest;
import com.sport_pro_be.modules.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.modules.auth.dto.RegisterRequest;
import com.sport_pro_be.modules.auth.enums.OtpType;
import com.sport_pro_be.modules.auth.interfaces.IAuthService;
import com.sport_pro_be.modules.auth.interfaces.IEmailService;
import com.sport_pro_be.modules.auth.interfaces.IJwtService;
import com.sport_pro_be.modules.auth.repository.OtpVerificationRepository;
import com.sport_pro_be.modules.auth.repository.RefreshTokenRepository;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import com.sport_pro_be.config.AuthProperties;
import com.sport_pro_be.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import static com.sport_pro_be.modules.auth.constant.AuthConstant.*;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final IJwtService jwtService;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    @Loggable(action = "REQUEST_OTP", module = "AUTH")
    public void requestRegistrationOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException(EMAIL_EXIST);
        }

        issueOtpForEmail(normalizedEmail, OtpType.REGISTER, LocalDateTime.now());
    }

    @Override
    @Transactional
    @Loggable(action = "REGISTER", module = "AUTH")
    public void register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException(EMAIL_EXIST);
        }

        OtpVerification latestOtp = otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, OtpType.REGISTER)
                .orElseThrow(() -> new BadRequestException(OTP_VERIFICATION_REQUIRED));

        if (!latestOtp.isOtpVerified()) {
            throw new BadRequestException(OTP_VERIFICATION_REQUIRED);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(true);
        userRepository.save(user);

        latestOtp.setOtpVerified(false);
        otpVerificationRepository.save(latestOtp);
    }

    @Override
    @Transactional
    @Loggable(action = "LOGIN", module = "AUTH")
    public AuthTokenPairResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));
        if (!user.isEmailVerified()) {
            throw new BadRequestException(EMAIL_NOT_VERIFIED);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public AuthTokenPairResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException(REFRESH_TOKEN_REQUIRED);
        }

        String tokenHash = hashRefreshToken(refreshToken);
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(REFRESH_TOKEN_INVALID_OR_EXPIRED));

        if (currentToken.isRevoked()) {
            if (currentToken.getReplacedByTokenHash() != null) {
                refreshTokenRepository.revokeActiveByUserId(currentToken.getUser().getId(), LocalDateTime.now());
                throw new ConflictException(REFRESH_TOKEN_REUSE_DETECTED);
            }
            throw new UnauthorizedException(REFRESH_TOKEN_REVOKED);
        }

        if (currentToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            currentToken.setRevoked(true);
            currentToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(currentToken);
            throw new UnauthorizedException(REFRESH_TOKEN_INVALID_OR_EXPIRED);
        }

        User user = currentToken.getUser();
        String newRawRefreshToken = generateRefreshTokenValue();
        String newRefreshHash = hashRefreshToken(newRawRefreshToken);

        currentToken.setRevoked(true);
        currentToken.setRevokedAt(LocalDateTime.now());
        currentToken.setReplacedByTokenHash(newRefreshHash);
        refreshTokenRepository.save(currentToken);

        RefreshToken rotatedToken = new RefreshToken();
        rotatedToken.setUser(user);
        rotatedToken.setTokenHash(newRefreshHash);
        rotatedToken.setExpiresAt(LocalDateTime.now().plusDays(authProperties.getRefreshTokenExpirationDays()));
        rotatedToken.setRevoked(false);
        refreshTokenRepository.save(rotatedToken);

        String accessToken = jwtService.generateAccessToken(user);
        return new AuthTokenPairResponse(
                TOKEN_TYPE_BEARER,
                accessToken,
                jwtService.getExpirationSeconds(),
                user.getEmail(),
                newRawRefreshToken);
    }

    @Override
    @Transactional
    @Loggable(action = "LOGOUT", module = "AUTH")
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String tokenHash = hashRefreshToken(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    if (!token.isRevoked()) {
                        token.setRevoked(true);
                        token.setRevokedAt(LocalDateTime.now());
                        refreshTokenRepository.save(token);
                    }
                });
    }

    @Override
    @Transactional
    public void verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        OtpVerification otp = otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, OtpType.REGISTER)
                .orElseThrow(() -> new BadRequestException(INVALID_OTP));

        if (otp.isUsed()) {
            throw new BadRequestException(INVALID_OTP);
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setUsed(true);
            otpVerificationRepository.save(otp);
            throw new BadRequestException(OTP_EXPIRED);
        }

        if (!otp.getOtpCode().equals(request.otp())) {
            int currentAttemptCount = otp.getAttemptCount() == null ? 0 : otp.getAttemptCount();
            int newAttemptCount = currentAttemptCount + 1;
            otp.setAttemptCount(newAttemptCount);
            if (newAttemptCount >= OTP_MAX_ATTEMPTS) {
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
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException(ACCOUNT_ALREADY_VERIFIED);
        }

        issueOtpForEmail(normalizedEmail, OtpType.REGISTER, LocalDateTime.now());
    }

    private void issueOtpForEmail(String normalizedEmail, OtpType otpType, LocalDateTime now) {
        otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(normalizedEmail, otpType)
                .ifPresent(lastOtp -> {
                    LocalDateTime nextAllowed = lastOtp.getCreatedAt()
                            .plusSeconds(authProperties.getOtpResendCooldownSeconds());
                    if (nextAllowed.isAfter(now)) {
                        throw new TooManyRequestsException(OTP_REQUEST_TOO_FREQUENT);
                    }
                });

        otpVerificationRepository.invalidateAllActiveByEmailAndType(normalizedEmail, otpType);

        String otpCode = generateOtpCode();
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(normalizedEmail);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setOtpType(otpType);
        otpVerification.setUsed(false);
        otpVerification.setAttemptCount(0);
        otpVerification.setOtpVerified(false);
        otpVerification.setExpiresAt(now.plusMinutes(authProperties.getOtpExpirationMinutes()));
        otpVerificationRepository.save(otpVerification);

        emailService.sendOtpEmail(normalizedEmail, otpCode, authProperties.getOtpExpirationMinutes());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateOtpCode() {
        int value = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private AuthTokenPairResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = generateRefreshTokenValue();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashRefreshToken(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(authProperties.getRefreshTokenExpirationDays()));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new AuthTokenPairResponse(
                TOKEN_TYPE_BEARER,
                accessToken,
                jwtService.getExpirationSeconds(),
                user.getEmail(),
                rawRefreshToken);
    }

    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(SHA_256_NOT_AVAILABLE, ex);
        }
    }
}

