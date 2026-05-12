package com.sport_pro_be.modules.brand.service;

import com.sport_pro_be.modules.brand.dto.BrandRequest;
import com.sport_pro_be.modules.brand.dto.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBrandService {
    BrandResponse createBrand(BrandRequest request);
    BrandResponse updateBrand(Long id, BrandRequest request);
    void deleteBrand(Long id);
    BrandResponse getBrandBySlug(String slug);
    Page<BrandResponse> getBrands(Pageable pageable, String search);
    void updateStatus(Long id, boolean isActive);
}

