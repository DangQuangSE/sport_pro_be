package com.sport_pro_be.modules.public_config.repository;

import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import com.sport_pro_be.modules.public_config.domain.SettingScope;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PublicConfigRepository extends JpaRepository<PublicConfig, Long> {
    Optional<PublicConfig> findByConfigKey(String configKey);
    boolean existsByConfigKey(String configKey);
    List<PublicConfig> findAllByOrderByCategoryAscConfigKeyAsc();
    List<PublicConfig> findAllByScopeAndActiveTrueOrderByCategoryAscConfigKeyAsc(SettingScope scope);
    Optional<PublicConfig> findByConfigKeyAndScopeAndActiveTrue(String configKey, SettingScope scope);

    default void deleteAndFlush(PublicConfig config) {
        delete(config);
        flush();
    }
}
