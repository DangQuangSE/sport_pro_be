package com.sport_pro_be.modules.brand.controller;

import com.sport_pro_be.modules.brand.constant.BrandConstant;
import com.sport_pro_be.modules.brand.dto.BrandResponse;
import com.sport_pro_be.modules.brand.service.IBrandService;
import com.sport_pro_be.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final IBrandService brandService;

    @GetMapping
    public ApiResponse<Page<BrandResponse>> getAllBrands(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search) {
        return ApiResponse.of(BrandConstant.GET_BRANDS_SUCCESS, brandService.getBrands(pageable, search));
    }

    @GetMapping("/{slug}")
    public ApiResponse<BrandResponse> getBrandBySlug(@PathVariable String slug) {
        return ApiResponse.of(BrandConstant.GET_BRAND_DETAIL_SUCCESS, brandService.getBrandBySlug(slug));
    }
}

