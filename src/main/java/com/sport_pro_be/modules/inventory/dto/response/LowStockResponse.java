package com.sport_pro_be.modules.inventory.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LowStockResponse {

    private Long variantId;
    private String sku;
    private String productName;
    private String size;
    private String color;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
}
