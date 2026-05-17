package com.sport_pro_be.modules.auth.repository;

import com.sport_pro_be.modules.auth.enums.OtpType;
import com.sport_pro_be.modules.auth.domain.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailIgnoreCaseAndOtpTypeOrderByCreatedAtDesc(String email, OtpType otpType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OtpVerification o set o.used = true where lower(o.email) = lower(:email) and o.otpType = :otpType and o.used = false")
    int invalidateAllActiveByEmailAndType(@Param("email") String email, @Param("otpType") OtpType otpType);
}

