package com.sport_pro_be.modules.membership.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Builder
public record MembershipTierResponse(
        Long id,
        String code,
        Map<String, String> displayNames,
        Map<String, String> descriptions,
        BigDecimal threshold,
        Integer sortOrder,
        BigDecimal discountPercentage,
        boolean freeShipping,
        Map<String, Object> benefits,
        boolean active,
        Long version) {
}
