package com.sport_pro_be.modules.printing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingPriceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/printing/price-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrintingPriceConfigController {

    private final IPrintingPriceConfigService priceConfigService;

    @GetMapping
    public ApiResponse<List<PrintingPriceConfig>> getAllPriceConfigs() {
        return ApiResponse.of(null, priceConfigService.getAllPriceConfigs());
    }

    @PostMapping
    public ApiResponse<PrintingPriceConfig> createPriceConfig(@Valid @RequestBody PrintingDto.PriceConfigRequest request) {
        return ApiResponse.of(PrintingMessageConstant.CREATE_SUCCESS, priceConfigService.createPriceConfig(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PrintingPriceConfig> updatePriceConfig(@PathVariable Long id, @Valid @RequestBody PrintingDto.PriceConfigRequest request) {
        return ApiResponse.of(PrintingMessageConstant.UPDATE_SUCCESS, priceConfigService.updatePriceConfig(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePriceConfig(@PathVariable Long id) {
        priceConfigService.deletePriceConfig(id);
        return ApiResponse.of(PrintingMessageConstant.DELETE_SUCCESS, null);
    }
}
