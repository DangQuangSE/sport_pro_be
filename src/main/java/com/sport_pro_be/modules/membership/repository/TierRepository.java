package com.sport_pro_be.modules.membership.repository;

import com.sport_pro_be.modules.membership.domain.TierConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TierRepository extends JpaRepository<TierConfig, Long> {
    List<TierConfig> findAllByOrderByThresholdDesc();
}
