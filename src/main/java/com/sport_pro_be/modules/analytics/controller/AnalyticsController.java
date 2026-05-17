package com.sport_pro_be.modules.analytics.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.analytics.constant.AnalyticsMessageConstant;
import com.sport_pro_be.modules.analytics.dto.OrderStatsResponse;
import com.sport_pro_be.modules.analytics.dto.RevenueReportResponse;
import com.sport_pro_be.modules.analytics.dto.TopProductResponse;
import com.sport_pro_be.modules.analytics.dto.TrendingDesignResponse;
import com.sport_pro_be.modules.analytics.interfaces.IAnalyticsService;
import com.sport_pro_be.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final IAnalyticsService analyticsService;

    @GetMapping("/revenue")
    public ApiResponse<List<RevenueReportResponse>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        if (start.isAfter(end)) {
            throw new AppException(AnalyticsMessageConstant.INVALID_DATE_RANGE, HttpStatus.BAD_REQUEST);
        }
        
        List<RevenueReportResponse> data = analyticsService.getDailyRevenue(start, end);
        return ApiResponse.of(AnalyticsMessageConstant.GET_REVENUE_SUCCESS, data);
    }

    @GetMapping("/top-products")
    public ApiResponse<List<TopProductResponse>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<TopProductResponse> data = analyticsService.getTopSellingProducts(limit);
        return ApiResponse.of(AnalyticsMessageConstant.GET_TOP_PRODUCTS_SUCCESS, data);
    }

    @GetMapping("/trending-designs")
    public ApiResponse<List<TrendingDesignResponse>> getTrendingDesigns(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<TrendingDesignResponse> data = analyticsService.getTrendingDesigns(limit);
        return ApiResponse.of(AnalyticsMessageConstant.GET_TRENDING_DESIGNS_SUCCESS, data);
    }

    @GetMapping("/order-stats")
    public ApiResponse<OrderStatsResponse> getOrderStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        if (start.isAfter(end)) {
            throw new AppException(AnalyticsMessageConstant.INVALID_DATE_RANGE, HttpStatus.BAD_REQUEST);
        }
        
        OrderStatsResponse data = analyticsService.getOrderStatistics(start, end);
        return ApiResponse.of(AnalyticsMessageConstant.GET_ORDER_STATS_SUCCESS, data);
    }
}
