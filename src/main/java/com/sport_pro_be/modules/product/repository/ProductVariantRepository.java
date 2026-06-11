package com.sport_pro_be.modules.product.repository;

import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    List<ProductVariant> findByProductIdAndStatusNot(Long productId, ProductStatus status);
}


