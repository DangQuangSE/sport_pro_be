package com.sport_pro_be.modules.product.dto.request;

import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantRequest {
    @NotBlank(message = ProductMessageConstant.SKU_REQUIRED)
    private String sku;

    @NotBlank(message = ProductMessageConstant.SIZE_REQUIRED)
    private String size;

    @NotNull(message = "Color selection is required")
    private Long colorId;

    @Positive(message = ProductMessageConstant.PRICE_POSITIVE)
    @NotNull(message = ProductMessageConstant.PRICE_REQUIRED)
    private BigDecimal originalPrice;

    private BigDecimal salePrice;

    @Min(value = 0, message = ProductMessageConstant.STOCK_NON_NEGATIVE)
    private Integer stockQuantity = 0;

    private ProductStatus status = ProductStatus.ACTIVE;
}


