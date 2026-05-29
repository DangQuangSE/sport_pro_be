package com.sport_pro_be.modules.product.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductListResponse {
    private Long id;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private String brandName;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private List<String> availableSizes;
    private List<String> availableColors;

    // Frontend compatibility fields
    private String sku;
    private BigDecimal basePrice;
    private String imageUrl;
    private String gender;
    private String status;
    private Integer totalStock;
    private Double averageRating;
    private Boolean isFeatured;
    private Boolean isCustomizable;
}


