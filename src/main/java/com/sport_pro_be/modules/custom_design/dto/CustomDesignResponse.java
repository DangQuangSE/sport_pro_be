package com.sport_pro_be.modules.custom_design.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomDesignResponse {

    private Long id;
    private String designImageUrl;
    private String backDesignImageUrl;
    private String designMetadata;
    private Long printingMaterialId;
    private String printingMaterialName;
    private Integer numTextLines;
    private Integer numImages;
    private BigDecimal totalPrintingPrice;
    private LocalDateTime createdAt;
}

