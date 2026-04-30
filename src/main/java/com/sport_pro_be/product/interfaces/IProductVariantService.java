package com.sport_pro_be.product.interfaces;

import com.sport_pro_be.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.product.dto.response.ProductVariantResponse;

public interface IProductVariantService {
    ProductVariantResponse createVariant(Long productId, ProductVariantRequest request);
    ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request);
    void deleteVariant(Long variantId);
}
