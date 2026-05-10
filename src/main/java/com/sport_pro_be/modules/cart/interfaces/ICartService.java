package com.sport_pro_be.modules.cart.interfaces;

import com.sport_pro_be.modules.cart.dto.request.CartItemRequest;
import com.sport_pro_be.modules.cart.dto.response.CartResponse;

public interface ICartService {
    CartResponse getMyCart(Long userId);
    CartResponse addOrUpdateItem(Long userId, CartItemRequest request);
    void removeItem(Long userId, Long cartItemId);
}
