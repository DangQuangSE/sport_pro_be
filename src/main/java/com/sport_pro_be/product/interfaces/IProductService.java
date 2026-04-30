package com.sport_pro_be.product.interfaces;

import com.sport_pro_be.product.constant.Gender;
import com.sport_pro_be.product.constant.ProductStatus;
import com.sport_pro_be.product.dto.request.ProductCreateRequest;
import com.sport_pro_be.product.dto.request.ProductUpdateRequest;
import com.sport_pro_be.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IProductService {
    ProductDetailResponse createProduct(ProductCreateRequest request);
    ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request);
    void deleteProduct(Long id);
    Page<ProductListResponse> getProducts(Long categoryId, Long brandId, Gender gender, String size, String color, BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status, Pageable pageable);
    ProductDetailResponse getProductById(Long id);
    ProductDetailResponse getProductBySlug(String slug);
}
