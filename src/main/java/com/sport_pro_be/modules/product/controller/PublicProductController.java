package com.sport_pro_be.modules.product.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.modules.product.dto.response.ProductListResponse;
import com.sport_pro_be.modules.product.interfaces.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final IProductService productService;

    @Operation(
            summary = "Get product list for users (Public Product List)",
            description = "Supports dynamic keyword searching, and filtering by Category, Brand, Gender, Size, Color, and Price range. " +
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
                    description = "Filter by featured products. True to retrieve featured products, false for others.",
                    required = false
            )
            @RequestParam(required = false) Boolean isFeatured,

            @ParameterObject
            @org.springframework.data.web.PageableDefault(size = 12, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductListResponse> products = productService.getProducts(keyword, categoryId, brandId, gender, size, color, minPrice, maxPrice, isFeatured, ProductStatus.ACTIVE, false, pageable);
        return ApiResponse.of(ProductMessageConstant.SUCCESS, products);
    }

    @GetMapping("/{slug}")
    public ApiResponse<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ApiResponse.of(ProductMessageConstant.SUCCESS, productService.getProductBySlug(slug));
    }
}



