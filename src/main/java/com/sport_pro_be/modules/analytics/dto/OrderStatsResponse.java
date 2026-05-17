package com.sport_pro_be.modules.analytics.dto;

import com.sport_pro_be.modules.order.enums.OrderStatus;
import java.util.Map;

public record OrderStatsResponse(Long totalOrders, Map<OrderStatus, Long> statusCounts) {
}
