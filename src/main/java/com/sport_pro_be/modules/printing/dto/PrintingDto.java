package com.sport_pro_be.modules.printing.dto;

import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.enums.PrintingElementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

public class PrintingDto {

    @Data
    public static class MaterialRequest {
        @NotBlank(message = PrintingMessageConstant.NAME_REQUIRED)
        private String name;
        
        private String description;
        
        @NotNull(message = PrintingMessageConstant.BASE_PRICE_REQUIRED)
        @PositiveOrZero(message = PrintingMessageConstant.BASE_PRICE_POSITIVE)
        private BigDecimal basePrice;
        
        private Boolean isActive = true;
    }

    @Data
    public static class PriceConfigRequest {
        @NotNull(message = PrintingMessageConstant.TYPE_REQUIRED)
        private PrintingElementType type;
        
        @NotNull(message = PrintingMessageConstant.UNIT_PRICE_REQUIRED)
        @PositiveOrZero(message = PrintingMessageConstant.UNIT_PRICE_POSITIVE)
        private BigDecimal unitPrice;
        
        private String description;
    }
}
