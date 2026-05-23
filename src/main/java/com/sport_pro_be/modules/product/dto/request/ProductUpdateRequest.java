package com.sport_pro_be.modules.product.dto.request;

import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequest {
    @NotBlank(message = ProductMessageConstant.NAME_REQUIRED)
    private String name;

    private String description;

    @NotNull(message = ProductMessageConstant.CATEGORY_ID_REQUIRED)
    private Long categoryId;

    @NotNull(message = ProductMessageConstant.BRAND_ID_REQUIRED)
    private Long brandId;

    @NotNull(message = ProductMessageConstant.GENDER_REQUIRED)
    private Gender gender;

    @NotNull(message = ProductMessageConstant.STATUS_REQUIRED)
    private ProductStatus status;

    private Boolean isFeatured;
}


