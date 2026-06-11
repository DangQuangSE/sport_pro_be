package com.sport_pro_be.modules.product.interfaces;

import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.dto.request.ProductCreateRequest;
import com.sport_pro_be.modules.product.dto.request.ProductUpdateRequest;
import com.sport_pro_be.modules.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.modules.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IProductService {
    ProductDetailResponse createProduct(ProductCreateRequest request);
    ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request);
    void deleteProduct(Long id);
    void restoreProduct(Long id);
    Page<ProductListResponse> getProducts(String keyword, Long categoryId, Long brandId, Gender gender, String size, String color, BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, ProductStatus status, Boolean includeDeleted, Pageable pageable);
    ProductDetailResponse getProductById(Long id);
    ProductDetailResponse getProductBySlug(String slug);
}


