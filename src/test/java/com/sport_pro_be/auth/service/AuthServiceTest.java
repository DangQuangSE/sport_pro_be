package com.sport_pro_be.auth.service;

import com.sport_pro_be.auth.domain.OtpType;
import com.sport_pro_be.auth.domain.OtpVerification;
import com.sport_pro_be.auth.domain.RefreshToken;
import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.dto.AuthTokenPairResponse;
import com.sport_pro_be.auth.dto.RegisterRequest;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.interfaces.IEmailService;
import com.sport_pro_be.auth.interfaces.IJwtService;
import com.sport_pro_be.auth.repository.OtpVerificationRepository;
import com.sport_pro_be.auth.repository.RefreshTokenRepository;
import com.sport_pro_be.auth.repository.UserRepository;
import com.sport_pro_be.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.sport_pro_be.auth.constant.AuthConstant.OTP_SENT_SUCCESS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_INCORRECT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_LOCKED_TOO_MANY_ATTEMPTS;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_VERIFICATION_REQUIRED;
import static com.sport_pro_be.auth.constant.AuthConstant.REFRESH_TOKEN_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private IJwtService jwtService;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        lenient().when(authProperties.getOtpExpirationMinutes()).thenReturn(5L);
        lenient().when(authProperties.getOtpResendCooldownSeconds()).thenReturn(0L);
        lenient().when(authProperties.getRefreshTokenExpirationDays()).thenReturn(14L);
    }

    @Test
    void verifyOtp_whenOtpMismatch_shouldIncreaseAttemptAndKeepOtpActive() {
        String email = "user@example.com";
        OtpVerification latestOtp = buildOtp(email, "123456", 0, false, LocalDateTime.now().plusMinutes(5));

        when(otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(email, OtpType.REGISTER)).thenReturn(Optional.of(latestOtp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.verifyOtp(new OtpVerifyRequest(email, "999999")));

        assertEquals(OTP_INCORRECT, exception.getReason());
        assertEquals(1, latestOtp.getAttemptCount());
        assertFalse(latestOtp.isUsed());
        verify(otpVerificationRepository).save(latestOtp);
    }

    @Test
    void verifyOtp_whenReachedMaxAttempts_shouldLockOtp() {
        String email = "user@example.com";
        OtpVerification latestOtp = buildOtp(email, "123456", 4, false, LocalDateTime.now().plusMinutes(5));

        when(otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(email, OtpType.REGISTER)).thenReturn(Optional.of(latestOtp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.verifyOtp(new OtpVerifyRequest(email, "000000")));

        assertEquals(OTP_LOCKED_TOO_MANY_ATTEMPTS, exception.getReason());
        assertEquals(5, latestOtp.getAttemptCount());
        assertTrue(latestOtp.isUsed());
        verify(otpVerificationRepository).save(latestOtp);
    }

    @Test
    void requestRegistrationOtp_whenEmailAvailable_shouldIssueOtpAndSendMail() {
        String email = "user@example.com";

        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(email, OtpType.REGISTER)).thenReturn(Optional.empty());
        when(otpVerificationRepository.save(any(OtpVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.requestRegistrationOtp(email);
        assertEquals(OTP_SENT_SUCCESS, response.message());

        verify(otpVerificationRepository).invalidateAllActiveByEmailAndType(email, OtpType.REGISTER);

        ArgumentCaptor<OtpVerification> otpCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(otpCaptor.capture());
        OtpVerification savedOtp = otpCaptor.getValue();

        assertEquals(email, savedOtp.getEmail());
        assertEquals(0, savedOtp.getAttemptCount());
        assertFalse(savedOtp.isUsed());
        assertFalse(savedOtp.isOtpVerified());
        assertNotNull(savedOtp.getOtpCode());
        assertEquals(6, savedOtp.getOtpCode().length());

        verify(emailService).sendOtpEmail(email, savedOtp.getOtpCode(), 5L);
    }

    @Test
    void register_whenOtpNotVerified_shouldReject() {
        String email = "user@example.com";
        OtpVerification latestOtp = buildOtp(email, "123456", 0, true, LocalDateTime.now().plusMinutes(5));
        latestOtp.setOtpVerified(false);

        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(email, OtpType.REGISTER)).thenReturn(Optional.of(latestOtp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.register(new RegisterRequest(email, "password123")));

        assertEquals(OTP_VERIFICATION_REQUIRED, exception.getReason());
    }

    @Test
    void register_whenOtpVerified_shouldCreateUser() {
        String email = "user@example.com";
        OtpVerification latestOtp = buildOtp(email, "123456", 0, true, LocalDateTime.now().plusMinutes(5));
        latestOtp.setOtpVerified(true);

        when(userRepository.existsByEmailIgnoreCase(email)).thenReturn(false);
        when(otpVerificationRepository.findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(email, OtpType.REGISTER)).thenReturn(Optional.of(latestOtp));
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        authService.register(new RegisterRequest(email, "password123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(email, savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertTrue(savedUser.isEmailVerified());

        verify(otpVerificationRepository).save(eq(latestOtp));
        assertFalse(latestOtp.isOtpVerified());
    }

    @Test
    void refreshAccessToken_whenRefreshTokenMissing_shouldFail() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.refreshAccessToken(null));

        assertEquals(REFRESH_TOKEN_REQUIRED, exception.getReason());
    }

    @Test
    void refreshAccessToken_whenRefreshTokenValid_shouldRotateAndReturnTokenPair() {
        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");

        RefreshToken current = new RefreshToken();
        current.setUser(user);
        current.setRevoked(false);
        current.setExpiresAt(LocalDateTime.now().plusDays(1));

        String rawRefreshToken = "raw-refresh-token";
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(current));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.getExpirationSeconds()).thenReturn(900L);

        AuthTokenPairResponse result = authService.refreshAccessToken(rawRefreshToken);

        assertEquals("Bearer", result.tokenType());
        assertEquals("access-token", result.accessToken());
        assertEquals(900L, result.expiresInSeconds());
        assertEquals("user@example.com", result.email());
        assertNotNull(result.refreshToken());
        assertTrue(current.isRevoked());
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
    }

    @Test
    void logout_whenTokenFound_shouldRevokeToken() {
        User user = new User();
        user.setId(11L);
        RefreshToken existing = new RefreshToken();
        existing.setUser(user);
        existing.setRevoked(false);

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(existing));

        authService.logout("refresh-token");

        assertTrue(existing.isRevoked());
        verify(refreshTokenRepository).save(existing);
    }

    private OtpVerification buildOtp(String email, String code, Integer attempts, boolean used, LocalDateTime expiresAt) {
        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setOtpType(OtpType.REGISTER);
        otp.setOtpCode(code);
        otp.setAttemptCount(attempts);
        otp.setUsed(used);
        otp.setExpiresAt(expiresAt);
        otp.setCreatedAt(LocalDateTime.now());
        return otp;
    }
}
