package com.sport_pro_be.modules.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.modules.product.dto.response.ProductListResponse;
import com.sport_pro_be.modules.product.interfaces.IProductService;
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
        return ApiResponse.of(ProductMessageConstant.SUCCESS, products);
    }

    @GetMapping("/{slug}")
    public ApiResponse<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ApiResponse.of(ProductMessageConstant.SUCCESS, productService.getProductBySlug(slug));
    }
}


