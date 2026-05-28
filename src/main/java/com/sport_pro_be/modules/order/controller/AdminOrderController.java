package com.sport_pro_be.modules.order.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.order.constant.OrderMessageConstant;
import com.sport_pro_be.modules.order.dto.OrderResponse;
import com.sport_pro_be.modules.order.dto.UpdateOrderStatusRequest;
import com.sport_pro_be.modules.order.interfaces.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final IOrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.sport_pro_be.modules.order.enums.OrderStatus status,
            Pageable pageable) {
        Page<OrderResponse> response = orderService.getAllOrders(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ALL_ORDERS_RETRIEVED, response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderDetailsAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ORDER_DETAILS_RETRIEVED, response));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.of(OrderMessageConstant.ORDER_STATUS_UPDATED, response));
    }
}
