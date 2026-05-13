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
}
