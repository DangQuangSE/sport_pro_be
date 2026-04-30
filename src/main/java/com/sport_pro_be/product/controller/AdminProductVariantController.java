package com.sport_pro_be.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.product.interfaces.IProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product-variants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductVariantController {
    
    private final IProductVariantService productVariantService;
    
    @PutMapping("/{variantId}")
    public ApiResponse<ProductVariantResponse> updateVariant(@PathVariable Long variantId, @Valid @RequestBody ProductVariantRequest request) {
        return ApiResponse.of("Variant updated successfully", productVariantService.updateVariant(variantId, request));
    }

    @DeleteMapping("/{variantId}")
    public ApiResponse<Void> deleteVariant(@PathVariable Long variantId) {
        productVariantService.deleteVariant(variantId);
        return ApiResponse.of("Variant deleted successfully", null);
    }
}
