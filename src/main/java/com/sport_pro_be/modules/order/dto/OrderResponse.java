package com.sport_pro_be.modules.order.dto;

import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.enums.PaymentMethod;
import com.sport_pro_be.modules.order.enums.PricingSnapshotStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String shippingAddress;
    private String phoneNumber;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal subtotalAmount;
    private BigDecimal printingAmount;
    private BigDecimal tierDiscountAmount;
    private BigDecimal couponDiscountAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private String appliedTierCode;
    private Map<String, Long> pricingRuleVersions;
    private PricingSnapshotStatus pricingSnapshotStatus;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
