package com.sport_pro_be.modules.inventory.dto.request;

import com.sport_pro_be.modules.inventory.constant.InventoryMessageConstant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestockRequest {

    @NotNull(message = InventoryMessageConstant.RESTOCK_QUANTITY_POSITIVE)
    @Min(value = 1, message = InventoryMessageConstant.RESTOCK_QUANTITY_POSITIVE)
    private Integer quantity;
}
