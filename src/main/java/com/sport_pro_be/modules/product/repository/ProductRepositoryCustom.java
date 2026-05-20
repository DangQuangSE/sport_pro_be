package com.sport_pro_be.modules.product.repository;

import com.sport_pro_be.modules.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ProductRepositoryCustom {
    Page<Product> findAllWithAssociations(Specification<Product> spec, Pageable pageable);
}