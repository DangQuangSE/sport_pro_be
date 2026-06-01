package com.sport_pro_be.modules.printing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.printing.domain.PrintingColor;
import com.sport_pro_be.modules.printing.dto.PrintingConfigResponse;
import com.sport_pro_be.modules.printing.interfaces.IPrintingColorService;
import com.sport_pro_be.modules.printing.interfaces.IPrintingMaterialService;
import com.sport_pro_be.modules.printing.interfaces.IPrintingPriceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/printing")
@RequiredArgsConstructor
public class PublicPrintingController {

    private final IPrintingMaterialService materialService;
    private final IPrintingPriceConfigService priceConfigService;
    private final IPrintingColorService colorService;

    @GetMapping("/all")
    public ApiResponse<PrintingConfigResponse> getFullConfigs() {
        return ApiResponse.of(null, PrintingConfigResponse.builder()
                .materials(materialService.getActiveMaterials())
                .priceConfigs(priceConfigService.getAllPriceConfigs())
                .colors(colorService.getActiveColors())
                .build());
    }

    @GetMapping("/colors")
    public ApiResponse<List<PrintingColor>> getActiveColors() {
        return ApiResponse.of(null, colorService.getActiveColors());
    }
}
