package com.sport_pro_be.modules.analytics.service;

import com.sport_pro_be.modules.analytics.dto.OrderStatsResponse;
import com.sport_pro_be.modules.analytics.dto.RevenueReportResponse;
import com.sport_pro_be.modules.analytics.dto.TopProductResponse;
import com.sport_pro_be.modules.analytics.dto.TrendingDesignResponse;
import com.sport_pro_be.modules.analytics.interfaces.IAnalyticsService;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.repository.OrderItemRepository;
import com.sport_pro_be.modules.order.repository.OrderRepository;
import com.sport_pro_be.modules.custom_design.repository.CustomDesignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService implements IAnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomDesignRepository customDesignRepository;

    @Override
    public List<RevenueReportResponse> getDailyRevenue(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return orderRepository.calculateDailyRevenue(startDateTime, endDateTime);
    }

    @Override
    public List<TopProductResponse> getTopSellingProducts(int limit) {
        return orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }

    @Override
    public List<TrendingDesignResponse> getTrendingDesigns(int limit) {
        return customDesignRepository.findTrendingDesigns(PageRequest.of(0, limit));
    }

    @Override
    public OrderStatsResponse getOrderStatistics(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        
        List<Object[]> results = orderRepository.countOrdersByStatus(startDateTime, endDateTime);
        
        Map<OrderStatus, Long> statusCounts = new HashMap<>();
        long totalOrders = 0;
        
        for (Object[] result : results) {
            OrderStatus status = (OrderStatus) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
            totalOrders += count;
        }
        
        return new OrderStatsResponse(totalOrders, statusCounts);
    }
}
