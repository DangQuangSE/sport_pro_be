package com.sport_pro_be.modules.product.dto.response;

import com.sport_pro_be.modules.product.enums.ProductStatus;
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
    private Long colorId;
    private String colorName;
    private String colorHex;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private ProductStatus status;
}


