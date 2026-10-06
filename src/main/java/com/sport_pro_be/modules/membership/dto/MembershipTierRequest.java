package com.sport_pro_be.modules.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipTierRequest {
    private String code;
    private Map<String, String> displayNames;
    private Map<String, String> descriptions;
    private BigDecimal threshold;
    private Integer sortOrder;
    private BigDecimal discountPercentage;
    private Boolean freeShipping;
    private Map<String, Object> benefits;
    private Boolean active;
    private Long expectedVersion;
}
