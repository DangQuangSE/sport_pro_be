package com.sport_pro_be.modules.public_config.dto.request;

import lombok.*;
import com.sport_pro_be.modules.public_config.domain.SettingScope;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicConfigUpdateRequest {
    private String configValue;
    private String description;
    private String category;
    private String unit;
    private SettingScope scope;
    private Boolean active;
    private String validationRules;

    @NotNull(message = "expectedVersion is required")
    private Long expectedVersion;
}
