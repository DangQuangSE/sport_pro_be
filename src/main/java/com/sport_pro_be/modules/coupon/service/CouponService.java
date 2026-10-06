package com.sport_pro_be.modules.coupon.service;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.coupon.constant.CouponMessageConstant;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.interfaces.ICouponService;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.repository.OrderRepository;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import com.sport_pro_be.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService implements ICouponService {

    private static final List<OrderStatus> USAGE_EXCLUDED_STATUSES =
            List.of(OrderStatus.CANCELLED, OrderStatus.RETURNED, OrderStatus.REFUNDED);

    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final ITierService tierService;

    @Override
    @Transactional
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

        MembershipTier requiredTier = coupon.getRequiredMembershipTier();
        if (requiredTier == null && coupon.getRequiredTier() != null) {
            requiredTier = tierService.getTierByCode(coupon.getRequiredTier().name());
        }
        if (requiredTier != null && !tierService.isTierAtLeast(user, requiredTier)) {
            throw new BadRequestException(String.format(CouponMessageConstant.TIER_NOT_REACHED, requiredTier.getCode()));
        }

        if (coupon.getMaxUsagePerUser() != null) {
            long userUsageCount = orderRepository.countByUserIdAndCouponIdAndStatusNotIn(
                    user.getId(), coupon.getId(), USAGE_EXCLUDED_STATUSES);
            if (userUsageCount >= coupon.getMaxUsagePerUser()) {
                // Per-user check is a plain SELECT COUNT, not atomic like the global usedCount
                // increment - two concurrent requests from the same user could both pass this
                // check in a rare race. Accepted as low-risk debt; this log line gives forensic
                // visibility if abuse is ever suspected.
                log.info("Coupon {} rejected for user {}: per-user usage limit reached ({}/{})",
                        coupon.getCode(), user.getId(), userUsageCount, coupon.getMaxUsagePerUser());
                throw new BadRequestException(CouponMessageConstant.USER_USAGE_LIMIT_REACHED);
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

    @Override
    @Transactional
    public boolean incrementUsage(Long couponId) {
        return couponRepository.incrementUsageIfBelowLimit(couponId) > 0;
    }
}
