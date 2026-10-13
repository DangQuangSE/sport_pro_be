package com.sport_pro_be.modules.public_config.repository;

import com.sport_pro_be.modules.public_config.domain.SettingChangeAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingChangeAuditRepository extends JpaRepository<SettingChangeAudit, Long> {
}
