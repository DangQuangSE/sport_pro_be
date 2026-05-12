package com.sport_pro_be.modules.coupon.dto;

import com.sport_pro_be.modules.auth.enums.UserTier;
import com.sport_pro_be.modules.coupon.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private UserTier requiredTier;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private boolean isActive = true;
}
