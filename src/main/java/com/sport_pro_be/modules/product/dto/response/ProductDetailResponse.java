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
    private String categoryName;
    private Gender gender;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
}


