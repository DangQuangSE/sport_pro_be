package com.sport_pro_be.modules.product.interfaces;

import com.sport_pro_be.modules.product.dto.request.ProductImageRequest;
import com.sport_pro_be.modules.product.dto.response.ProductImageResponse;

public interface IProductImageService {
    ProductImageResponse addImage(Long productId, ProductImageRequest request);
    void deleteImage(Long imageId);
}


