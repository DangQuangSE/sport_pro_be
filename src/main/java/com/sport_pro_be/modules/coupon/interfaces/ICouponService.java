package com.sport_pro_be.modules.coupon.interfaces;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import java.math.BigDecimal;

public interface ICouponService {
    Coupon validateAndGetCoupon(String code, User user, BigDecimal orderAmount);
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount);

    /**
     * Atomically increments usedCount if the coupon is still below its usageLimit.
     * Returns false (no-op) if the limit was already reached - the caller must treat
     * that as a rejection, not silently proceed.
     */
    boolean incrementUsage(Long couponId);
}
