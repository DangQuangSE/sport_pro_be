package com.sport_pro_be.modules.printing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingMaterialService;
import com.sport_pro_be.modules.printing.interfaces.IPrintingPriceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/printing")
@RequiredArgsConstructor
public class AdminPrintingController {

    private final IPrintingMaterialService materialService;
    private final IPrintingPriceConfigService priceConfigService;

    // --- Materials ---
    @GetMapping("/materials")
    public ApiResponse<List<PrintingMaterial>> getAllMaterials() {
        return ApiResponse.of(null, materialService.getAllMaterials());
    }

    @PostMapping("/materials")
    public ApiResponse<PrintingMaterial> createMaterial(@Valid @RequestBody PrintingDto.MaterialRequest request) {
        return ApiResponse.of(PrintingMessageConstant.CREATE_SUCCESS, materialService.createMaterial(request));
    }

    @PutMapping("/materials/{id}")
    public ApiResponse<PrintingMaterial> updateMaterial(@PathVariable Long id, @Valid @RequestBody PrintingDto.MaterialRequest request) {
        return ApiResponse.of(PrintingMessageConstant.UPDATE_SUCCESS, materialService.updateMaterial(id, request));
    }

    @DeleteMapping("/materials/{id}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ApiResponse.of(PrintingMessageConstant.DELETE_SUCCESS, null);
    }

    // --- Price Configs ---
    @GetMapping("/price-configs")
    public ApiResponse<List<PrintingPriceConfig>> getAllPriceConfigs() {
        return ApiResponse.of(null, priceConfigService.getAllPriceConfigs());
    }

    @PostMapping("/price-configs")
    public ApiResponse<PrintingPriceConfig> createPriceConfig(@Valid @RequestBody PrintingDto.PriceConfigRequest request) {
        return ApiResponse.of(PrintingMessageConstant.CREATE_SUCCESS, priceConfigService.createPriceConfig(request));
    }

    @PutMapping("/price-configs/{id}")
    public ApiResponse<PrintingPriceConfig> updatePriceConfig(@PathVariable Long id, @Valid @RequestBody PrintingDto.PriceConfigRequest request) {
        return ApiResponse.of(PrintingMessageConstant.UPDATE_SUCCESS, priceConfigService.updatePriceConfig(id, request));
    }

    @DeleteMapping("/price-configs/{id}")
    public ApiResponse<Void> deletePriceConfig(@PathVariable Long id) {
        priceConfigService.deletePriceConfig(id);
        return ApiResponse.of(PrintingMessageConstant.DELETE_SUCCESS, null);
    }
}
