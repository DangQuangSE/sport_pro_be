package com.sport_pro_be.modules.analytics.interfaces;

import com.sport_pro_be.modules.analytics.dto.OrderStatsResponse;
import com.sport_pro_be.modules.analytics.dto.RevenueReportResponse;
import com.sport_pro_be.modules.analytics.dto.TopProductResponse;
import com.sport_pro_be.modules.analytics.dto.TrendingDesignResponse;

import java.time.LocalDate;
import java.util.List;

public interface IAnalyticsService {
    List<RevenueReportResponse> getDailyRevenue(LocalDate start, LocalDate end);
    List<TopProductResponse> getTopSellingProducts(int limit);
    List<TrendingDesignResponse> getTrendingDesigns(int limit);
    OrderStatsResponse getOrderStatistics(LocalDate start, LocalDate end);
}
