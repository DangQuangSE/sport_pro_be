package com.sport_pro_be.modules.inventory.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.inventory.constant.InventoryMessageConstant;
import com.sport_pro_be.modules.inventory.dto.request.RestockRequest;
import com.sport_pro_be.modules.inventory.dto.response.LowStockResponse;
import com.sport_pro_be.modules.inventory.interfaces.IInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final IInventoryService inventoryService;

    @GetMapping("/low-stock")
    public ApiResponse<List<LowStockResponse>> getLowStockVariants() {
        return ApiResponse.of(InventoryMessageConstant.LOW_STOCK_RETRIEVED,
                inventoryService.getLowStockVariants());
    }

    @PutMapping("/restock/{variantId}")
    public ApiResponse<LowStockResponse> restock(
            @PathVariable Long variantId,
            @Valid @RequestBody RestockRequest request) {
        return ApiResponse.of(InventoryMessageConstant.STOCK_ADJUSTED,
                inventoryService.restock(variantId, request.getQuantity()));
    }
}
