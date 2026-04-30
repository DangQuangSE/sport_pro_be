package com.sport_pro_be.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.product.enums.Gender;
import com.sport_pro_be.product.constant.ProductMessageConstant;
import com.sport_pro_be.product.enums.ProductStatus;
import com.sport_pro_be.product.dto.request.ProductCreateRequest;
import com.sport_pro_be.product.dto.request.ProductImageRequest;
import com.sport_pro_be.product.dto.request.ProductUpdateRequest;
import com.sport_pro_be.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.product.dto.response.ProductImageResponse;
import com.sport_pro_be.product.dto.response.ProductListResponse;
import com.sport_pro_be.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.product.interfaces.IProductImageService;
import com.sport_pro_be.product.interfaces.IProductService;
import com.sport_pro_be.product.interfaces.IProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final IProductService productService;
    private final IProductVariantService productVariantService;
    private final IProductImageService productImageService;

    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status,
            Pageable pageable) {
        Page<ProductListResponse> products = productService.getProducts(categoryId, brandId, gender, size, color, minPrice, maxPrice, status, pageable);
        return ApiResponse.of(ProductMessageConstant.SUCCESS, products);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.of(ProductMessageConstant.SUCCESS, productService.getProductById(id));
    }

    @PostMapping
    public ApiResponse<ProductDetailResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.of(ProductMessageConstant.PRODUCT_CREATED, productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return ApiResponse.of(ProductMessageConstant.PRODUCT_UPDATED, productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.of(ProductMessageConstant.PRODUCT_DELETED, null);
    }

    // Variants
    @PostMapping("/{productId}/variants")
    public ApiResponse<ProductVariantResponse> createVariant(@PathVariable Long productId, @Valid @RequestBody ProductVariantRequest request) {
        return ApiResponse.of(ProductMessageConstant.VARIANT_CREATED, productVariantService.createVariant(productId, request));
    }

    // Images
    @PostMapping("/{productId}/images")
    public ApiResponse<ProductImageResponse> addImage(@PathVariable Long productId, @Valid @RequestBody ProductImageRequest request) {
        return ApiResponse.of(ProductMessageConstant.IMAGE_ADDED, productImageService.addImage(productId, request));
    }
}

