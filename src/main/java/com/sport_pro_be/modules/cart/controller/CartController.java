package com.sport_pro_be.modules.cart.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.cart.constant.CartMessageConstant;
import com.sport_pro_be.modules.cart.dto.request.CartItemRequest;
import com.sport_pro_be.modules.cart.dto.response.CartResponse;
import com.sport_pro_be.modules.cart.interfaces.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        CartResponse response = cartService.getMyCart(userId);
        return ResponseEntity.ok(ApiResponse.of(CartMessageConstant.CART_RETRIEVED, response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addOrUpdateItem(@Valid @RequestBody CartItemRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CartResponse response = cartService.addOrUpdateItem(userId, request);
        return ResponseEntity.ok(ApiResponse.of(CartMessageConstant.CART_UPDATED, response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.of(CartMessageConstant.ITEM_REMOVED, null));
    }
}
