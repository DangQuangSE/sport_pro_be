package com.sport_pro_be.product.dto.request;

import com.sport_pro_be.product.constant.Gender;
import com.sport_pro_be.product.constant.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductUpdateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Status is required")
    private ProductStatus status;
}
