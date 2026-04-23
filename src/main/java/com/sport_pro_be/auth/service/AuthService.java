package com.sport_pro_be.auth.service;

import com.sport_pro_be.auth.domain.EmailOtp;
import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.dto.ApiMessageResponse;
import com.sport_pro_be.auth.dto.LoginRequest;
import com.sport_pro_be.auth.dto.LoginSuccessResponse;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.dto.RegisterRequest;
import com.sport_pro_be.auth.interfaces.IAuthService;
import com.sport_pro_be.auth.interfaces.IEmailService;
import com.sport_pro_be.auth.interfaces.IJwtService;
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

import static com.sport_pro_be.auth.constant.AuthConstant.ACCOUNT_ALREADY_VERIFIED;
import static com.sport_pro_be.auth.constant.AuthConstant.EMAIL_EXIST;
import static com.sport_pro_be.auth.constant.AuthConstant.EMAIL_NOT_VERIFIED;
import static com.sport_pro_be.auth.constant.AuthConstant.INVALID_CREDENTIALS;
import static com.sport_pro_be.auth.constant.AuthConstant.INVALID_OTP;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_EXPIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_INCORRECT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_LOCKED_TOO_MANY_ATTEMPTS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_MAX_ATTEMPTS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_REQUEST_TOO_FREQUENT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_RESENT_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_SENT_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_VERIFICATION_REQUIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_VERIFIED_FOR_REGISTRATION;
import static com.sport_pro_be.auth.constant.AuthConstant.REGISTRATION_SUCCESS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
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
    public LoginSuccessResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(BAD_REQUEST, EMAIL_NOT_VERIFIED);
        }

        String token = jwtService.generateAccessToken(user);
        return new LoginSuccessResponse("Bearer", token, jwtService.getExpirationSeconds(), user.getEmail());
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
                    LocalDateTime nextAllowed = lastOtp.getCreatedAt().plusSeconds(authProperties.getOtpResendCooldownSeconds());
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
}
