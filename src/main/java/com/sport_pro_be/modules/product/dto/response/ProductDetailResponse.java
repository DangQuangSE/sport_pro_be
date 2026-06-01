package com.sport_pro_be.modules.product.dto.response;

import com.sport_pro_be.modules.product.enums.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String brandName;
    private Long brandId;
    private String categoryName;
    private Long categoryId;
    private Gender gender;
    private com.sport_pro_be.modules.product.enums.ProductStatus status;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    private Boolean isFeatured;
    private Boolean isCustomizable;
}


