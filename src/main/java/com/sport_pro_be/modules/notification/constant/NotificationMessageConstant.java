package com.sport_pro_be.modules.notification.constant;

public final class NotificationMessageConstant {

    private NotificationMessageConstant() {
    }

    // Validation Messages
    public static final String BOT_TOKEN_REQUIRED =
            "Bot token is required when Discord notifications are enabled";
    public static final String ORDER_CHANNEL_REQUIRED =
            "Order channel ID is required when Discord notifications are enabled";
    public static final String ALERT_CHANNEL_REQUIRED =
            "Alert channel ID is required when Discord notifications are enabled";
    public static final String ADMIN_ORDER_URL_TEMPLATE_REQUIRED =
            "Admin order URL template containing {orderId} is required when Discord notifications are enabled";
    public static final String HTTP_TIMEOUTS_INVALID =
            "HTTP timeouts must be positive and shorter than the processing lease";
    public static final String PRODUCT_NAME_REQUIRED = "Product name is required";
    public static final String QUANTITY_MIN = "Quantity must be at least 1";
    public static final String SNAPSHOT_SCHEMA_VERSION_MIN = "Snapshot schema version must be at least 1";
    public static final String ORDER_ID_REQUIRED = "Order ID is required";
    public static final String ORDER_CODE_REQUIRED = "Order code is required";
    public static final String ORDER_CREATED_AT_REQUIRED = "Order creation time is required";
    public static final String TOTAL_AMOUNT_REQUIRED = "Total amount is required";
    public static final String TOTAL_AMOUNT_MIN = "Total amount must be positive or zero";
    public static final String CURRENCY_REQUIRED = "Currency is required";
    public static final String ORDER_LINES_REQUIRED = "Order lines are required";
}
