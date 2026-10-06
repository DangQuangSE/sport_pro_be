package com.sport_pro_be.modules.pricing.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Checkout context only. Monetary values are intentionally absent because the
 * backend recalculates the quote from current database state.
 */
@Data
public class CheckoutQuoteRequest {

    private List<Long> cartItemIds;

    @Size(max = 50)
    private String couponCode;

    /** Optional context retained for future carrier/region rules. */
    @Size(max = 2000)
    private String shippingAddress;
}
