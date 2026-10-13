package com.sport_pro_be.modules.public_config.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "public_configs", indexes = {
        @Index(name = "idx_public_config_key", columnList = "config_key"),
        @Index(name = "idx_public_configs_scope_active", columnList = "scope, active"),
        @Index(name = "idx_public_configs_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicConfig extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false, length = 50)
    private ConfigType configType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "category", nullable = false, length = 40)
    @Builder.Default
    private String category = "CONTENT";

    @Column(name = "unit", length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 12)
    @Builder.Default
    private SettingScope scope = SettingScope.INTERNAL;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = false;

    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "updated_by")
    private Long updatedBy;
}
