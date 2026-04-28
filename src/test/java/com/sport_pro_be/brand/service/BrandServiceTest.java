package com.sport_pro_be.brand.service;

import com.sport_pro_be.brand.domain.Brand;
import com.sport_pro_be.brand.dto.BrandRequest;
import com.sport_pro_be.brand.dto.BrandResponse;
import com.sport_pro_be.brand.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void createBrand_whenSlugExists_shouldAppendSuffix() {
        BrandRequest request = new BrandRequest("Adidas", null, null, null, null);

        when(brandRepository.existsByNameIgnoreCase("Adidas")).thenReturn(false);
        when(brandRepository.existsBySlug("adidas")).thenReturn(true);
        when(brandRepository.existsBySlug("adidas-1")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponse response = brandService.createBrand(request);

        assertEquals("adidas-1", response.getSlug());
        ArgumentCaptor<Brand> brandCaptor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).save(brandCaptor.capture());
        assertEquals("adidas-1", brandCaptor.getValue().getSlug());
    }
}
