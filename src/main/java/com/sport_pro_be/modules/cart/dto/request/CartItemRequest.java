package com.sport_pro_be.modules.cart.dto.request;

import com.sport_pro_be.modules.cart.constant.CartMessageConstant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    @NotNull(message = CartMessageConstant.VARIANT_ID_REQUIRED)
    private Long variantId;

    @NotNull(message = CartMessageConstant.QUANTITY_REQUIRED)
    @Min(value = 0, message = CartMessageConstant.QUANTITY_MIN)
    private Integer quantity;

    // Optional: attach a custom design to this cart item
    private Long customDesignId;
}
