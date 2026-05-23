package com.sport_pro_be.modules.color.dto.request;

import com.sport_pro_be.modules.color.constant.ColorMessageConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColorRequest {
    @NotBlank(message = ColorMessageConstant.COLOR_NAME_REQUIRED)
    private String name;

    @NotBlank(message = ColorMessageConstant.COLOR_HEX_REQUIRED)
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = ColorMessageConstant.COLOR_HEX_INVALID)
    private String hexCode;
}
