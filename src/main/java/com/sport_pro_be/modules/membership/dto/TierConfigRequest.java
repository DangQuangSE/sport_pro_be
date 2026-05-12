package com.sport_pro_be.modules.membership.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TierConfigRequest {
    private BigDecimal threshold;
    private String description;
}
