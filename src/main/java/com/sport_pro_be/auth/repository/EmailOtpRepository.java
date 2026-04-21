package com.sport_pro_be.auth.repository;

import com.sport_pro_be.auth.domain.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    Optional<EmailOtp> findTopByEmailIgnoreCaseAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(String email, String otpCode);
}
