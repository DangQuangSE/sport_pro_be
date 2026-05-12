package com.sport_pro_be.modules.order.dto;

import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String shippingAddress;
    private String phoneNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
