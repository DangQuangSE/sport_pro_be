package com.sport_pro_be.modules.order.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.order.constant.OrderMessageConstant;
import com.sport_pro_be.modules.order.dto.OrderRequest;
import com.sport_pro_be.modules.order.dto.OrderResponse;
import com.sport_pro_be.modules.order.interfaces.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody OrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ORDER_PLACED_SUCCESS, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<OrderResponse> response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.USER_ORDERS_RETRIEVED, response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderResponse response = orderService.getOrderDetails(userId, orderId);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ORDER_DETAILS_RETRIEVED, response));
    }
}
