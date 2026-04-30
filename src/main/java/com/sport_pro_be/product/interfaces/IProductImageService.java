package com.sport_pro_be.product.interfaces;

import com.sport_pro_be.product.dto.request.ProductImageRequest;
import com.sport_pro_be.product.dto.response.ProductImageResponse;

public interface IProductImageService {
    ProductImageResponse addImage(Long productId, ProductImageRequest request);
    void deleteImage(Long imageId);
}

