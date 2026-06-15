package com.sport_pro_be.modules.public_config.dto.response;

import com.sport_pro_be.modules.public_config.domain.ConfigType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private ConfigType configType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
