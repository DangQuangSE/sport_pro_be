package com.sport_pro_be.modules.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private String productName;
    private String productSlug;
    private String size;
    private String color;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer quantity;
    private BigDecimal itemTotal;
    private Long customDesignId;
    private String designImageUrl;
    private BigDecimal printingPrice;
    private Boolean isCustomizable;
    private String productImageUrl;
}
