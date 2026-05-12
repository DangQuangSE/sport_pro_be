package com.sport_pro_be.modules.brand.dto;

import com.sport_pro_be.modules.brand.constant.BrandConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = BrandConstant.NAME_CANNOT_BE_BLANK)
    @Size(min = 2, max = 100, message = BrandConstant.NAME_SIZE)
    private String name;

    private String description;
    private String logoUrl;
    private String country;
    private String websiteUrl;
    private Boolean isActive;
}

