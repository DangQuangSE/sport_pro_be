package com.sport_pro_be.modules.product.repository;

import com.sport_pro_be.modules.product.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    List<ProductVariant> findByProductId(Long productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.lowStockThreshold IS NOT NULL AND v.stockQuantity <= v.lowStockThreshold")
    List<ProductVariant> findLowStockVariants();
}



