package com.sport_pro_be.modules.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String sku;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal unitPrice;
    // Custom design info (null if no design attached)
    private Long customDesignId;
    private String designImageUrl;
    private String backDesignImageUrl;
    private BigDecimal printingPrice;
    private BigDecimal printingAmount;
    private Boolean isReviewed;
}

