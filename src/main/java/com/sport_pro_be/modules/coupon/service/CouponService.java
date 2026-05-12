package com.sport_pro_be.modules.coupon.service;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.coupon.constant.CouponMessageConstant;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.interfaces.ICouponService;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public Coupon validateAndGetCoupon(String code, User user, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrueAndIsDeletedFalse(code)
                .orElseThrow(() -> new BadRequestException(CouponMessageConstant.INVALID_COUPON));

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new BadRequestException(CouponMessageConstant.COUPON_NOT_ACTIVE);
        }
        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new BadRequestException(CouponMessageConstant.COUPON_EXPIRED);
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException(CouponMessageConstant.USAGE_LIMIT_REACHED);
        }

        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException(CouponMessageConstant.MIN_AMOUNT_NOT_REACHED);
        }

        if (coupon.getRequiredTier() != null) {
            if (user.getTier().ordinal() < coupon.getRequiredTier().ordinal()) {
                throw new BadRequestException(String.format(CouponMessageConstant.TIER_NOT_REACHED, coupon.getRequiredTier()));
            }
        }

        return coupon;
    }

    @Override
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
        
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount;
    }
}
