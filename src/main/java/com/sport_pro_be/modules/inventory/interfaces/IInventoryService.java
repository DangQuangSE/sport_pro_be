package com.sport_pro_be.modules.inventory.interfaces;

import com.sport_pro_be.modules.inventory.dto.response.LowStockResponse;

import java.util.List;

public interface IInventoryService {

    void deductStock(Long variantId, int quantity);

    void restoreStock(Long variantId, int quantity);

    LowStockResponse restock(Long variantId, int quantity);

    List<LowStockResponse> getLowStockVariants();
}
