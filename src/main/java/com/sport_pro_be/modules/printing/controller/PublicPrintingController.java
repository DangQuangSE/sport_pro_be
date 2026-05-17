package com.sport_pro_be.modules.printing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.printing.dto.PrintingConfigResponse;
import com.sport_pro_be.modules.printing.interfaces.IPrintingMaterialService;
import com.sport_pro_be.modules.printing.interfaces.IPrintingPriceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/printing")
@RequiredArgsConstructor
public class PublicPrintingController {

    private final IPrintingMaterialService materialService;
    private final IPrintingPriceConfigService priceConfigService;

    @GetMapping("/all")
    public ApiResponse<PrintingConfigResponse> getFullConfigs() {
        return ApiResponse.of(null, PrintingConfigResponse.builder()
                .materials(materialService.getActiveMaterials())
                .priceConfigs(priceConfigService.getAllPriceConfigs())
                .build());
    }
}
