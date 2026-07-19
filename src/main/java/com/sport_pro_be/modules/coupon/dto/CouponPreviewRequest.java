package com.sport_pro_be.modules.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponPreviewRequest {
    @NotBlank
    private String code;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal orderAmount;
}
