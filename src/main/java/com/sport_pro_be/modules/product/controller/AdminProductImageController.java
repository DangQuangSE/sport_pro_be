package com.sport_pro_be.modules.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.interfaces.IProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product-images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductImageController {

    private final IProductImageService productImageService;

    @DeleteMapping("/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ApiResponse.of(ProductMessageConstant.IMAGE_DELETED, null);
    }
}


