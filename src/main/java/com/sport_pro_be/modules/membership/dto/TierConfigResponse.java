package com.sport_pro_be.modules.membership.dto;

import com.sport_pro_be.modules.auth.enums.UserTier;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TierConfigResponse {
    private Long id;
    private UserTier tier;
    private BigDecimal threshold;
    private String description;
}
