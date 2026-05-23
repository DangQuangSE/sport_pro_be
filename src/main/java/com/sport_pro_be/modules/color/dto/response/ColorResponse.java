package com.sport_pro_be.modules.color.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ColorResponse {
    private Long id;
    private String name;
    private String hexCode;
}
