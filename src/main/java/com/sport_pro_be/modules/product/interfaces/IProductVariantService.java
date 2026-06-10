package com.sport_pro_be.modules.product.interfaces;

import com.sport_pro_be.modules.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.modules.product.dto.response.ProductVariantResponse;
import java.util.List;

public interface IProductVariantService {
    ProductVariantResponse createVariant(Long productId, ProductVariantRequest request);
    ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request);
    void deleteVariant(Long variantId);
    List<ProductVariantResponse> createVariantsBatch(Long productId, List<ProductVariantRequest> requests);
}


