package com.sport_pro_be.modules.custom_design.dto;

import com.sport_pro_be.modules.custom_design.constant.CustomDesignMessageConstant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomDesignRequest {

    @NotNull(message = CustomDesignMessageConstant.MATERIAL_ID_REQUIRED)
    private Long materialId;

    @NotNull(message = CustomDesignMessageConstant.NUM_TEXT_LINES_REQUIRED)
    @Min(value = 0, message = CustomDesignMessageConstant.NUM_TEXT_LINES_MIN)
    private Integer numTextLines;

    @NotNull(message = CustomDesignMessageConstant.NUM_IMAGES_REQUIRED)
    @Min(value = 0, message = CustomDesignMessageConstant.NUM_IMAGES_MIN)
    private Integer numImages;

    // Raw JSON string — tọa độ, font, màu sắc (optional, để có thể load lại editor)
    private String metadata;
}
