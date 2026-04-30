package com.sport_pro_be.product.dto.response;

import com.sport_pro_be.product.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private String size;
    private String color;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private ProductStatus status;
}

