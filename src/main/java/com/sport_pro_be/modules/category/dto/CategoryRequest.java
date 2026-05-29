package com.sport_pro_be.modules.category.dto;

import com.sport_pro_be.modules.category.constant.CategoryConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = CategoryConstant.NAME_CANNOT_BE_BLANK)
    @Size(min = 2, max = 100, message = CategoryConstant.NAME_SIZE)
    private String name;

    private String description;
    private Long parentId;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Boolean isCustomizable;
}

