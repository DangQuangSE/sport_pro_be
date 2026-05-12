package com.sport_pro_be.modules.brand.repository;

import com.sport_pro_be.modules.brand.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
    org.springframework.data.domain.Page<Brand> findAllByNameContainingIgnoreCaseAndIsActiveTrue(String name, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Brand> findAllByIsActiveTrue(org.springframework.data.domain.Pageable pageable);
}

