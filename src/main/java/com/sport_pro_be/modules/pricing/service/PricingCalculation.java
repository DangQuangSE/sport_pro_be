package com.sport_pro_be.modules.pricing.service;

import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.pricing.dto.PricingBreakdownResponse;

import java.util.List;

public record PricingCalculation(
        List<PricingLine> lines,
        Coupon coupon,
        MembershipTier tier,
        PricingBreakdownResponse breakdown) {
}
