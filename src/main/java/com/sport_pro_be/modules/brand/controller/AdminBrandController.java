package com.sport_pro_be.modules.brand.controller;

import com.sport_pro_be.modules.brand.constant.BrandConstant;
import com.sport_pro_be.modules.brand.dto.BrandRequest;
import com.sport_pro_be.modules.brand.dto.BrandResponse;
import com.sport_pro_be.modules.brand.service.IBrandService;
import com.sport_pro_be.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final IBrandService brandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        return ApiResponse.of(BrandConstant.CREATE_BRAND_SUCCESS, brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        return ApiResponse.of(BrandConstant.UPDATE_BRAND_SUCCESS, brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.of(BrandConstant.DELETE_BRAND_SUCCESS, null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        brandService.updateStatus(id, isActive);
        return ApiResponse.of(BrandConstant.UPDATE_STATUS_SUCCESS, null);
    }
}

