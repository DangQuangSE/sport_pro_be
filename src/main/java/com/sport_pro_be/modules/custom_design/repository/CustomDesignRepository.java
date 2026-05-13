package com.sport_pro_be.modules.custom_design.repository;

import com.sport_pro_be.modules.custom_design.domain.CustomDesign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomDesignRepository extends JpaRepository<CustomDesign, Long> {

    Page<CustomDesign> findByUserId(Long userId, Pageable pageable);

    Optional<CustomDesign> findByIdAndUserId(Long id, Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT new com.sport_pro_be.modules.analytics.dto.TrendingDesignResponse(cd.id, cd.designImageUrl, COUNT(oi)) " +
           "FROM OrderItem oi " +
           "JOIN oi.customDesign cd " +
           "GROUP BY cd.id, cd.designImageUrl " +
           "ORDER BY COUNT(oi) DESC")
    java.util.List<com.sport_pro_be.modules.analytics.dto.TrendingDesignResponse> findTrendingDesigns(org.springframework.data.domain.Pageable pageable);
}
