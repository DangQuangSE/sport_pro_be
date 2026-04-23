package com.sport_pro_be.auth.repository;

import com.sport_pro_be.auth.domain.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EmailOtp e set e.used = true where lower(e.email) = lower(:email) and e.used = false")
    int invalidateAllActiveByEmail(@Param("email") String email);
}
