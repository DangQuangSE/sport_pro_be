package com.sport_pro_be.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportResponse(LocalDate date, BigDecimal revenue) {
}
