package com.sport_pro_be.modules.product.repository;

import com.sport_pro_be.modules.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductRepositoryCustom {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithAssociations(@Param("id") Long id);
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.slug = :slug")
    Optional<Product> findBySlugWithAssociations(@Param("slug") String slug);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.slug = :slug OR p.slug LIKE CONCAT(:slug, '-%')")
    long countBySlugPattern(@Param("slug") String slug);
}