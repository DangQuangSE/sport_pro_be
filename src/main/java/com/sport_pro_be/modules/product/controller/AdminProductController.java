package com.sport_pro_be.modules.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.dto.request.ProductCreateRequest;
import com.sport_pro_be.modules.product.dto.request.ProductUpdateRequest;
import com.sport_pro_be.modules.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.modules.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.modules.product.dto.response.ProductImageResponse;
import com.sport_pro_be.modules.product.dto.response.ProductListResponse;
import com.sport_pro_be.modules.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.modules.product.interfaces.IProductImageService;
import com.sport_pro_be.modules.product.interfaces.IProductService;
import com.sport_pro_be.modules.product.interfaces.IProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status,
            Pageable pageable) {
        Page<ProductListResponse> products = productService.getProducts(keyword, categoryId, brandId, gender, size, color, minPrice, maxPrice, status, pageable);
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
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductImageResponse> addImage(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false, defaultValue = "false") Boolean isThumbnail,
            @RequestParam(required = false, defaultValue = "0") Integer sortOrder) {
        return ApiResponse.of(ProductMessageConstant.IMAGE_ADDED, productImageService.addImage(productId, file, variantId, isThumbnail, sortOrder));
    }
}


