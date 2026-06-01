package com.sport_pro_be.modules.auth.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record UserProfileResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    String avatar,
    String role,
    String tier,
    BigDecimal totalSpending
) {}
