package com.sport_pro_be.modules.public_config.dto.request;

import com.sport_pro_be.modules.public_config.domain.ConfigType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicConfigRequest {

    @NotBlank(message = "Config key cannot be blank")
    private String configKey;

    private String configValue;

    @NotNull(message = "Config type cannot be null")
    private ConfigType configType;

    private String description;
}
