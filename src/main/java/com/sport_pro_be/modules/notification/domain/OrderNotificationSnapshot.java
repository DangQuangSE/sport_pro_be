package com.sport_pro_be.modules.notification.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationSnapshot {

    @Min(value = 1, message = SNAPSHOT_SCHEMA_VERSION_MIN)
    private int schemaVersion;

    @NotNull(message = ORDER_ID_REQUIRED)
    private Long orderId;

    @NotBlank(message = ORDER_CODE_REQUIRED)
    private String orderCode;

    @NotNull(message = ORDER_CREATED_AT_REQUIRED)
    private Instant createdAt;

    @NotNull(message = TOTAL_AMOUNT_REQUIRED)
    @DecimalMin(value = "0.00", message = TOTAL_AMOUNT_MIN)
    private BigDecimal totalAmount;

    @NotBlank(message = CURRENCY_REQUIRED)
    private String currency;

    @NotNull(message = ORDER_LINES_REQUIRED)
    @Valid
    private List<OrderLineNotificationSnapshot> lines = new ArrayList<>();
}
