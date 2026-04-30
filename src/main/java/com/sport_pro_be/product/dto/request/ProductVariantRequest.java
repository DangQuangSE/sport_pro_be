package com.sport_pro_be.product.dto.request;

import com.sport_pro_be.product.constant.ProductStatus;
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
    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Size is required")
    private String size;

    @NotBlank(message = "Color is required")
    private String color;

    @Positive(message = "Price must be greater than 0")
    @NotNull(message = "Price is required")
    private BigDecimal price;

    private BigDecimal salePrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    private ProductStatus status = ProductStatus.ACTIVE;
}
