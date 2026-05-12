package com.sport_pro_be.modules.order.interfaces;

import com.sport_pro_be.modules.order.dto.OrderRequest;
import com.sport_pro_be.modules.order.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sport_pro_be.modules.order.enums.OrderStatus;

public interface IOrderService {
    OrderResponse placeOrder(Long userId, OrderRequest request);
    Page<OrderResponse> getUserOrders(Long userId, Pageable pageable);
    OrderResponse getOrderDetails(Long userId, Long orderId);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse getOrderDetailsAdmin(Long orderId);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
}
