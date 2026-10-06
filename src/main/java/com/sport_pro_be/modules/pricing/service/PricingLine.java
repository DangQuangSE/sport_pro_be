package com.sport_pro_be.modules.pricing.service;

import com.sport_pro_be.modules.cart.domain.CartItem;

import java.math.BigDecimal;

/** A priced cart line kept for order item snapshot creation. */
public record PricingLine(
        CartItem cartItem,
        BigDecimal unitPrice,
        BigDecimal printingAmount) {
}
