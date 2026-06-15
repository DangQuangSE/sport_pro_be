package com.sport_pro_be.modules.public_config.repository;

import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PublicConfigRepository extends JpaRepository<PublicConfig, Long> {
    Optional<PublicConfig> findByConfigKey(String configKey);
    boolean existsByConfigKey(String configKey);
}
