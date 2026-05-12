package com.sport_pro_be.modules.printing.dto;

import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintingConfigResponse {
    private List<PrintingMaterial> materials;
    private List<PrintingPriceConfig> priceConfigs;
}
