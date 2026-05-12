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
    private List<String> availableSizes;
    private List<String> availableColors;
}


