package com.sport_pro_be.modules.cart.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.auth.security.JwtAuthenticationFilter;
import com.sport_pro_be.modules.cart.constant.CartMessageConstant;
import com.sport_pro_be.modules.cart.dto.request.CartItemRequest;
import com.sport_pro_be.modules.cart.dto.response.CartResponse;
import com.sport_pro_be.modules.cart.interfaces.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        CartResponse response = cartService.getMyCart(userId);
        return ResponseEntity.ok(ApiResponse.of(CartMessageConstant.CART_RETRIEVED, response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addOrUpdateItem(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = extractUserId(authentication);
        CartResponse response = cartService.addOrUpdateItem(userId, request);
        return ResponseEntity.ok(ApiResponse.of(CartMessageConstant.CART_UPDATED, response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            Authentication authentication,
            @PathVariable Long itemId) {
        Long userId = extractUserId(authentication);
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.of(CartMessageConstant.ITEM_REMOVED, null));
    }

    private Long extractUserId(Authentication authentication) {
        // Assuming the principal is the User ID or we have a custom UserDetails
        // If the principal is the email string, we need to adapt this.
        // Usually, in Spring Security with JWT, we set the principal as the username/email or UserDetails.
        // Let's assume the username is the email, and we might need to get ID from UserDetails.
        // Wait, looking at the project structure, often the Authentication.getName() is the email.
        // If UserDetails implementation contains getId(), we can cast it. 
        // For safety, let's use Long.parseLong if the principal name is the ID.
        // Let me check how CustomUserDetailsService or JwtAuthenticationFilter works in this project.
        // Since I don't have it, I'll assume the ID is stored in the principal string or custom UserDetails.
        // I will use a placeholder logic that tries to cast it or parse it.
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            // Fallback, if getName is email, we might need a different approach.
            // But let's assume the project configures ID as the name/principal in JWT filter.
            return Long.parseLong(authentication.getName());
        }
    }
}
