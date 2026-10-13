package com.sport_pro_be.modules.pricing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.pricing.dto.CheckoutQuoteRequest;
import com.sport_pro_be.modules.pricing.dto.PricingBreakdownResponse;
import com.sport_pro_be.modules.pricing.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CheckoutController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<PricingBreakdownResponse>> quote(
            @Valid @RequestBody CheckoutQuoteRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                "Checkout quote calculated successfully",
                pricingService.calculate(
                        SecurityUtils.getCurrentUserId(),
                        request.getCartItemIds(),
                        request.getCouponCode()).breakdown()));
    }
}
