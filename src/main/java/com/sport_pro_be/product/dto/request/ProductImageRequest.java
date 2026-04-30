package com.sport_pro_be.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageRequest {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private Boolean isThumbnail = false;
    
    private Integer sortOrder = 0;
    
    private Long variantId;
}
