package com.sport_pro_be.modules.category.repository;

import com.sport_pro_be.modules.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Category> findAllByIsActiveTrueOrderByDisplayOrderAsc();
    List<Category> findAllByParentIdOrderByDisplayOrderAsc(Long parentId);
    org.springframework.data.domain.Page<Category> findAllByNameContainingIgnoreCaseAndIsActiveTrue(String name, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Category> findAllByIsActiveTrue(org.springframework.data.domain.Pageable pageable);
}

