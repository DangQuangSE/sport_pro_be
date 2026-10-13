package com.sport_pro_be.modules.membership.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record MembershipProfileResponse(
        MembershipTierResponse currentTier,
        MembershipTierResponse nextTier,
        List<MembershipTierResponse> activeTiers,
        BigDecimal totalSpending,
        BigDecimal amountToNextTier,
        BigDecimal progressPercentage) {
}
