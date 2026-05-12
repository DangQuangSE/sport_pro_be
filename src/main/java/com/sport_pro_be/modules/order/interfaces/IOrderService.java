package com.sport_pro_be.modules.order.interfaces;

import com.sport_pro_be.modules.order.dto.OrderRequest;
import com.sport_pro_be.modules.order.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    OrderResponse placeOrder(Long userId, OrderRequest request);
    Page<OrderResponse> getUserOrders(Long userId, Pageable pageable);
    OrderResponse getOrderDetails(Long userId, Long orderId);
}
