package com.sport_pro_be.modules.public_config.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingActivationRequest {

    @NotNull
    private Boolean active;

    @NotNull
    private Long expectedVersion;
}
