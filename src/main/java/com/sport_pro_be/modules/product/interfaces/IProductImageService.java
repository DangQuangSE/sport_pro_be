package com.sport_pro_be.modules.product.interfaces;

import com.sport_pro_be.modules.product.dto.request.ProductImageRequest;
import com.sport_pro_be.modules.product.dto.response.ProductImageResponse;

import org.springframework.web.multipart.MultipartFile;

public interface IProductImageService {
    ProductImageResponse addImage(Long productId, MultipartFile file, Long variantId, Boolean isThumbnail, Integer sortOrder);
    void deleteImage(Long imageId);
}


