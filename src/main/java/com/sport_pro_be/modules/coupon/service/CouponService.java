package com.sport_pro_be.modules.coupon.service;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public Coupon validateAndGetCoupon(String code, User user, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new BadRequestException("Invalid or expired coupon code"));

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new BadRequestException("Coupon is not yet active");
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new BadRequestException("Coupon has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit reached");
        }

        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount not reached for this coupon");
        }

        if (coupon.getRequiredTier() != null) {
            if (user.getTier().ordinal() < coupon.getRequiredTier().ordinal()) {
                throw new BadRequestException("Your membership tier is not high enough to use this coupon. Required: " + coupon.getRequiredTier());
            }
        }

        return coupon;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;
        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discount = coupon.getMaxDiscountAmount();
                }
                break;
            case FIXED_AMOUNT:
                discount = coupon.getDiscountValue();
                break;
            default:
                discount = BigDecimal.ZERO;
        }
        
        // Discount cannot exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount;
    }
}
