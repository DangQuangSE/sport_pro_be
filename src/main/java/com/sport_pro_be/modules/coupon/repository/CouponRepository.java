package com.sport_pro_be.modules.coupon.repository;

import com.sport_pro_be.modules.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndIsActiveTrueAndIsDeletedFalse(String code);
    
    Page<Coupon> findAllByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);
    
    java.util.Optional<Coupon> findByIdAndIsDeletedFalse(Long id);
}
