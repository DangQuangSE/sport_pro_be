package com.sport_pro_be.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.product.constant.Gender;
import com.sport_pro_be.product.constant.ProductStatus;
import com.sport_pro_be.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.product.dto.response.ProductListResponse;
import com.sport_pro_be.product.interfaces.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final IProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        
        // Force status to ACTIVE for public API
        Page<ProductListResponse> products = productService.getProducts(categoryId, brandId, gender, size, color, minPrice, maxPrice, ProductStatus.ACTIVE, pageable);
        return ApiResponse.of("Success", products);
    }

    @GetMapping("/{slug}")
    public ApiResponse<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ApiResponse.of("Success", productService.getProductBySlug(slug));
    }
}
