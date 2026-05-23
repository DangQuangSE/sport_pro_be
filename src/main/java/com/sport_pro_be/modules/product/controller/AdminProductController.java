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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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

    @Operation(
            summary = "Get all products list for administration (Admin Product List)",
            description = "Enables administrators to search and filter all products with any status (ACTIVE, INACTIVE, DELETED). " +
                          "All filters are OPTIONAL. Click 'Try it out' and then 'Execute' directly (without filling anything) to retrieve all existing mock database data."
    )
    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @Parameter(
                    description = "Search keyword for product name or description (e.g. shoe). Leave blank to retrieve all products.",
                    required = false
            )
            @RequestParam(required = false) String keyword,

            @Parameter(
                    description = "Category ID to filter by (e.g. 1). Leave blank to ignore category filter.",
                    required = false
            )
            @RequestParam(required = false) Long categoryId,

            @Parameter(
                    description = "Brand ID to filter by (e.g. 1). Leave blank to ignore brand filter.",
                    required = false
            )
            @RequestParam(required = false) Long brandId,

            @Parameter(
                    description = "Target gender of the product (MALE, FEMALE, UNISEX). Leave blank to retrieve all.",
                    required = false
            )
            @RequestParam(required = false) Gender gender,

            @Parameter(
                    description = "Product size to filter by (e.g. 42, S, M, L). Leave blank to ignore size filter.",
                    required = false
            )
            @RequestParam(value = "productSize", required = false) String size,

            @Parameter(
                    description = "Product color to filter by (e.g. Red, Black, White). Leave blank to ignore color filter.",
                    required = false
            )
            @RequestParam(required = false) String color,

            @Parameter(
                    description = "Minimum price in VND. Filters by sale price of variants. (Note: Existing DB mock data price is 100.00 VND). Leave blank to retrieve all.",
                    required = false
            )
            @RequestParam(required = false) BigDecimal minPrice,

            @Parameter(
                    description = "Maximum price in VND. Filters by sale price of variants. (e.g. 500 or 2000000). Leave blank to retrieve all.",
                    required = false
            )
            @RequestParam(required = false) BigDecimal maxPrice,

            @Parameter(
                    description = "Filter by product status (ACTIVE, INACTIVE, DELETED). Leave blank to ignore status filter.",
                    required = false
            )
            @RequestParam(required = false) ProductStatus status,

            @Parameter(
                    description = "Filter by featured products. True to retrieve featured products, false for others.",
                    required = false
            )
            @RequestParam(required = false) Boolean isFeatured,

            @ParameterObject
            @org.springframework.data.web.PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<ProductListResponse> products = productService.getProducts(keyword, categoryId, brandId, gender, size, color, minPrice, maxPrice, isFeatured, status, pageable);
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


