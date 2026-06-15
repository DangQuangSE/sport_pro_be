package com.sport_pro_be.modules.public_config.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicConfigUpdateRequest {
    private String configValue;
    private String description;
}
