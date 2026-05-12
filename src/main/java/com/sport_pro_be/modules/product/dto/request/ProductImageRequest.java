package com.sport_pro_be.modules.product.dto.request;

import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequest {
    @NotBlank(message = ProductMessageConstant.IMAGE_URL_REQUIRED)
    private String imageUrl;
    
    private Boolean isThumbnail = false;
    
    private Integer sortOrder = 0;
    
    private Long variantId;
}


