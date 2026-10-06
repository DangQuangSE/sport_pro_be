package com.sport_pro_be.modules.pricing.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record PricingBreakdownResponse(
        BigDecimal subtotalAmount,
        BigDecimal printingAmount,
        BigDecimal tierDiscountAmount,
        BigDecimal couponDiscountAmount,
        BigDecimal shippingAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String currency,
        String appliedTierCode,
        Map<String, Long> ruleVersions) {
}
