package com.sport_pro_be.auth.service;

import com.sport_pro_be.auth.domain.EmailOtp;
import com.sport_pro_be.auth.domain.User;
import com.sport_pro_be.auth.dto.OtpVerifyRequest;
import com.sport_pro_be.auth.interfaces.IEmailService;
import com.sport_pro_be.auth.interfaces.IJwtService;
import com.sport_pro_be.auth.repository.EmailOtpRepository;
import com.sport_pro_be.auth.repository.UserRepository;
import com.sport_pro_be.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.sport_pro_be.auth.constant.AuthConstant.OTP_INCORRECT;
import static com.sport_pro_be.auth.constant.AuthConstant.OTP_LOCKED_TOO_MANY_ATTEMPTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailOtpRepository emailOtpRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private IJwtService jwtService;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(authProperties.getOtpExpirationMinutes()).thenReturn(5L);
        when(authProperties.getOtpResendCooldownSeconds()).thenReturn(0L);
    }

    @Test
    void verifyOtp_whenOtpMismatch_shouldIncreaseAttemptAndKeepOtpActive() {
        String email = "user@example.com";
        EmailOtp latestOtp = buildOtp(email, "123456", 0, false, LocalDateTime.now().plusMinutes(5));

        when(emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)).thenReturn(Optional.of(latestOtp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.verifyOtp(new OtpVerifyRequest(email, "999999")));

        assertEquals(OTP_INCORRECT, exception.getReason());
        assertEquals(1, latestOtp.getAttemptCount());
        assertFalse(latestOtp.isUsed());
        verify(emailOtpRepository).save(latestOtp);
    }

    @Test
    void verifyOtp_whenReachedMaxAttempts_shouldLockOtp() {
        String email = "user@example.com";
        EmailOtp latestOtp = buildOtp(email, "123456", 4, false, LocalDateTime.now().plusMinutes(5));

        when(emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)).thenReturn(Optional.of(latestOtp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.verifyOtp(new OtpVerifyRequest(email, "000000")));

        assertEquals(OTP_LOCKED_TOO_MANY_ATTEMPTS, exception.getReason());
        assertEquals(5, latestOtp.getAttemptCount());
        assertTrue(latestOtp.isUsed());
        verify(emailOtpRepository).save(latestOtp);
    }

    @Test
    void resendOtp_whenUserNotVerified_shouldIssueNewOtpAndSendMail() {
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(false);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(emailOtpRepository.findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)).thenReturn(Optional.empty());
        when(emailOtpRepository.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resendOtp(email);

        verify(emailOtpRepository).invalidateAllActiveByEmail(email);

        ArgumentCaptor<EmailOtp> otpCaptor = ArgumentCaptor.forClass(EmailOtp.class);
        verify(emailOtpRepository).save(otpCaptor.capture());
        EmailOtp savedOtp = otpCaptor.getValue();

        assertEquals(email, savedOtp.getEmail());
        assertEquals(0, savedOtp.getAttemptCount());
        assertFalse(savedOtp.isUsed());
        assertNotNull(savedOtp.getOtpCode());
        assertEquals(6, savedOtp.getOtpCode().length());

        verify(emailService).sendOtpEmail(email, savedOtp.getOtpCode(), 5L);
    }

    private EmailOtp buildOtp(String email, String code, Integer attempts, boolean used, LocalDateTime expiresAt) {
        EmailOtp otp = new EmailOtp();
        otp.setEmail(email);
        otp.setOtpCode(code);
        otp.setAttemptCount(attempts);
        otp.setUsed(used);
        otp.setExpiresAt(expiresAt);
        otp.setCreatedAt(LocalDateTime.now());
        return otp;
    }
}
