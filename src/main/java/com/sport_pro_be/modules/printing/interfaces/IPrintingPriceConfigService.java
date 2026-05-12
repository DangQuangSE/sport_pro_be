package com.sport_pro_be.modules.printing.interfaces;

import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.dto.PrintingDto;

import java.util.List;

public interface IPrintingPriceConfigService {
    List<PrintingPriceConfig> getAllPriceConfigs();
    PrintingPriceConfig createPriceConfig(PrintingDto.PriceConfigRequest request);
    PrintingPriceConfig updatePriceConfig(Long id, PrintingDto.PriceConfigRequest request);
    void deletePriceConfig(Long id);
}
