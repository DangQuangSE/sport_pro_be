package com.sport_pro_be.modules.brand.service;

import com.sport_pro_be.modules.brand.constant.BrandConstant;
import com.sport_pro_be.modules.brand.domain.Brand;
import com.sport_pro_be.modules.brand.dto.BrandRequest;
import com.sport_pro_be.modules.brand.dto.BrandResponse;
import com.sport_pro_be.modules.brand.repository.BrandRepository;
import com.sport_pro_be.common.SlugUtils;
import com.sport_pro_be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BrandService implements IBrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        Brand brand = new Brand();
        mapRequestToEntity(request, brand);

        String slug = generateUniqueSlug(request.getName(), null);
        brand.setSlug(slug);

        Brand savedBrand = brandRepository.save(brand);
        return mapEntityToResponse(savedBrand);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(BrandConstant.BRAND_NOT_FOUND_ID, id)));

        if (!brand.getName().equals(request.getName())) {
            String slug = generateUniqueSlug(request.getName(), id);
            brand.setSlug(slug);
        }

        mapRequestToEntity(request, brand);
        Brand updatedBrand = brandRepository.save(brand);
        return mapEntityToResponse(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(BrandConstant.BRAND_NOT_FOUND_ID, id)));
        brand.setDeletedAt(LocalDateTime.now());
        brandRepository.save(brand);
    }

    @Override
    public BrandResponse getBrandBySlug(String slug) {
        return brandRepository.findBySlug(slug)
                .map(this::mapEntityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(BrandConstant.BRAND_NOT_FOUND_SLUG, slug)));
    }

    @Override
    public Page<BrandResponse> getBrands(Pageable pageable, String search) {
        Page<Brand> brandPage;
        if (search != null && !search.isBlank()) {
            brandPage = brandRepository.findAllByNameContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        } else {
            brandPage = brandRepository.findAllByIsActiveTrue(pageable);
        }
        return brandPage.map(this::mapEntityToResponse);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean isActive) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(BrandConstant.BRAND_NOT_FOUND_ID, id)));
        brand.setActive(isActive);
        brandRepository.save(brand);
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String baseSlug = SlugUtils.toSlugBase(name);
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            final String currentSlug = slug;
            boolean exists = brandRepository.findBySlug(currentSlug)
                    .map(b -> !b.getId().equals(currentId))
                    .orElse(false);

            if (!exists && !brandRepository.existsBySlug(currentSlug)) {
                return currentSlug;
            }

            slug = baseSlug + "-" + counter++;
        }
    }

    private void mapRequestToEntity(BrandRequest request, Brand brand) {
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setCountry(request.getCountry());
        brand.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getIsActive() != null) {
            brand.setActive(request.getIsActive());
        }
    }

    private BrandResponse mapEntityToResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .country(brand.getCountry())
                .websiteUrl(brand.getWebsiteUrl())
                .isActive(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}

