package com.sport_pro_be.auth.service;

import com.sport_pro_be.auth.domain.EmailOtp;
import com.sport_pro_be.auth.domain.RefreshToken;
import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.auth.dto.LoginRequest;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.dto.RegisterRequest;
import com.sport_pro_be.auth.interfaces.IAuthService;
import com.sport_pro_be.auth.interfaces.IEmailService;
import com.sport_pro_be.auth.interfaces.IJwtService;
import com.sport_pro_be.auth.repository.EmailOtpRepository;
import com.sport_pro_be.auth.repository.RefreshTokenRepository;
import com.sport_pro_be.auth.repository.UserRepository;
import com.sport_pro_be.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import static com.sport_pro_be.auth.constant.AuthConstant.ACCOUNT_ALREADY_VERIFIED;
import static com.sport_pro_be.auth.constant.AuthConstant.EMAIL_EXIST;
import static com.sport_pro_be.auth.constant.AuthConstant.EMAIL_NOT_VERIFIED;
import static com.sport_pro_be.auth.constant.AuthConstant.INVALID_CREDENTIALS;
import static com.sport_pro_be.auth.constant.AuthConstant.INVALID_OTP;
import static com.sport_pro_be.auth.constant.AuthConstant.LOGOUT_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_EXPIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_INCORRECT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_LOCKED_TOO_MANY_ATTEMPTS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_MAX_ATTEMPTS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_REQUEST_TOO_FREQUENT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_RESENT_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_SENT_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_VERIFICATION_REQUIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_VERIFIED_FOR_REGISTRATION;
import static com.sport_pro_be.auth.constant.AuthConstant.REFRESH_TOKEN_INVALID_OR_EXPIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.REFRESH_TOKEN_REQUIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.REFRESH_TOKEN_REUSE_DETECTED;
import static com.sport_pro_be.auth.constant.AuthConstant.REFRESH_TOKEN_REVOKED;
import static com.sport_pro_be.auth.constant.AuthConstant.REGISTRATION_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.SHA_256_NOT_AVAILABLE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final IJwtService jwtService;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public ApiMessageResponse requestRegistrationOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, EMAIL_EXIST);
        }

        issueOtpForEmail(normalizedEmail, LocalDateTime.now());
        return new ApiMessageResponse(OTP_SENT_SUCCESS);
    }

    @Override
    @Transactional
    public ApiMessageResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, EMAIL_EXIST);
        }

        EmailOtp latestOtp = emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, OTP_VERIFICATION_REQUIRED));

        if (!latestOtp.isOtpVerified()) {
            throw new ResponseStatusException(BAD_REQUEST, OTP_VERIFICATION_REQUIRED);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(true);
        userRepository.save(user);

        latestOtp.setOtpVerified(false);
        emailOtpRepository.save(latestOtp);

        return new ApiMessageResponse(REGISTRATION_SUCCESS);
    }

    @Override
    @Transactional
    public AuthTokenPairResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, INVALID_CREDENTIALS));
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(BAD_REQUEST, EMAIL_NOT_VERIFIED);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public AuthTokenPairResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, REFRESH_TOKEN_REQUIRED);
        }

        String tokenHash = hashRefreshToken(refreshToken);
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, REFRESH_TOKEN_INVALID_OR_EXPIRED));

        if (currentToken.isRevoked()) {
            if (currentToken.getReplacedByTokenHash() != null) {
                refreshTokenRepository.revokeActiveByUserId(currentToken.getUser().getId(), LocalDateTime.now());
                throw new ResponseStatusException(CONFLICT, REFRESH_TOKEN_REUSE_DETECTED);
            }
            throw new ResponseStatusException(UNAUTHORIZED, REFRESH_TOKEN_REVOKED);
        }

        if (currentToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            currentToken.setRevoked(true);
            currentToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(currentToken);
            throw new ResponseStatusException(UNAUTHORIZED, REFRESH_TOKEN_INVALID_OR_EXPIRED);
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
    public ApiMessageResponse logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return new ApiMessageResponse(LOGOUT_SUCCESS);
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

        return new ApiMessageResponse(LOGOUT_SUCCESS);
    }

    @Override
    @Transactional
    public ApiMessageResponse verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        EmailOtp otp = emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, INVALID_OTP));

        if (otp.isUsed()) {
            throw new ResponseStatusException(BAD_REQUEST, INVALID_OTP);
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setUsed(true);
            emailOtpRepository.save(otp);
            throw new ResponseStatusException(BAD_REQUEST, OTP_EXPIRED);
        }

        if (!otp.getOtpCode().equals(request.otp())) {
            int currentAttemptCount = otp.getAttemptCount() == null ? 0 : otp.getAttemptCount();
            int newAttemptCount = currentAttemptCount + 1;
            otp.setAttemptCount(newAttemptCount);
            if (newAttemptCount >= OTP_MAX_ATTEMPTS) {
                otp.setUsed(true);
                emailOtpRepository.save(otp);
                throw new ResponseStatusException(BAD_REQUEST, OTP_LOCKED_TOO_MANY_ATTEMPTS);
            }

            emailOtpRepository.save(otp);
            throw new ResponseStatusException(BAD_REQUEST, OTP_INCORRECT);
        }

        otp.setUsed(true);
        otp.setOtpVerified(true);
        emailOtpRepository.save(otp);

        return new ApiMessageResponse(OTP_VERIFIED_FOR_REGISTRATION);
    }

    @Override
    @Transactional
    public ApiMessageResponse resendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, ACCOUNT_ALREADY_VERIFIED);
        }

        issueOtpForEmail(normalizedEmail, LocalDateTime.now());
        return new ApiMessageResponse(OTP_RESENT_SUCCESS);
    }

    private void issueOtpForEmail(String normalizedEmail, LocalDateTime now) {
        emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(normalizedEmail)
                .ifPresent(lastOtp -> {
                    LocalDateTime nextAllowed = lastOtp.getCreatedAt()
                            .plusSeconds(authProperties.getOtpResendCooldownSeconds());
                    if (nextAllowed.isAfter(now)) {
                        throw new ResponseStatusException(TOO_MANY_REQUESTS,
                                OTP_REQUEST_TOO_FREQUENT);
                    }
                });

        emailOtpRepository.invalidateAllActiveByEmail(normalizedEmail);

        String otpCode = generateOtpCode();
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(normalizedEmail);
        emailOtp.setOtpCode(otpCode);
        emailOtp.setUsed(false);
        emailOtp.setAttemptCount(0);
        emailOtp.setOtpVerified(false);
        emailOtp.setExpiresAt(now.plusMinutes(authProperties.getOtpExpirationMinutes()));
        emailOtpRepository.save(emailOtp);

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
