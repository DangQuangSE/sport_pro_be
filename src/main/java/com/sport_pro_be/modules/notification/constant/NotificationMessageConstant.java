package com.sport_pro_be.modules.notification.constant;

public final class NotificationMessageConstant {

    private NotificationMessageConstant() {
    }

    // Validation Messages
    public static final String BOT_TOKEN_REQUIRED =
            "Bot token is required";
    public static final String ORDER_CHANNEL_REQUIRED =
            "A numeric order channel ID is required";
    public static final String ALERT_CHANNEL_REQUIRED =
            "A numeric alert channel ID is required";
    public static final String ADMIN_ORDER_URL_TEMPLATE_REQUIRED =
            "An absolute HTTP(S) admin order URL template containing {orderId} is required";
    public static final String HTTP_TIMEOUTS_INVALID =
            "HTTP timeouts must be positive and shorter than the processing lease";
    public static final String RETENTION_INVALID =
            "Sent retention must be at least 7 days and failed retention at least 30 days";
    public static final String CLEANUP_DELAY_INVALID = "Cleanup delay must be positive";
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
    public static final String DISCORD_RATE_LIMITED = "Discord rate limit response";
    public static final String DISCORD_UNAUTHORIZED = "Discord authentication failed";
    public static final String DISCORD_FORBIDDEN = "Discord channel permission denied";
    public static final String DISCORD_CHANNEL_NOT_FOUND = "Discord channel not found";
    public static final String DISCORD_CLIENT_ERROR = "Discord rejected the request";
    public static final String DISCORD_SERVER_ERROR = "Discord server error";
    public static final String DISCORD_NETWORK_ERROR = "Discord network error";
    public static final String DISCORD_INVALID_RESPONSE = "Discord returned an invalid response";
    public static final String DISCORD_MESSAGE_TOO_LONG = "A Discord message chunk exceeds 2000 characters";
    public static final String DELIVERY_FAILED = "Discord delivery failed";
    public static final String FINAL_LEASE_EXPIRED = "Processing lease expired after final delivery cycle";
    public static final String FAILURE_ALERT_TITLE = "Discord order notification failed";

    // Structured Log Events
    public static final String LOG_DISPATCH_ERROR =
            "notification_dispatch_error outboxId={} errorType={}";
    public static final String LOG_OWNERSHIP_LOST =
            "notification_ownership_lost outboxId={} transition={}";
    public static final String LOG_ALERT_TERMINAL =
            "notification_alert_terminal outboxId={} category={}";
    public static final String LOG_ORDER_TERMINAL =
            "notification_order_terminal outboxId={} category={}";
    public static final String LOG_ENQUEUED = "notification_enqueued outboxId={} orderId={}";
    public static final String LOG_CLAIMED = "notification_claimed outboxId={} cycle={}";
    public static final String LOG_SENT = "notification_sent outboxId={} chunks={}";
    public static final String LOG_RETRY_SCHEDULED =
            "notification_retry_scheduled outboxId={} notificationType={} cycle={} nextAttemptAt={} category={}";
    public static final String LOG_FAILED = "notification_failed outboxId={} category={}";
    public static final String LOG_CLEANED = "notification_cleaned sent={} failed={}";
    public static final String LOG_CLEANUP_ERROR =
            "notification_cleanup_error category={} errorType={}";
    public static final String LOG_QUEUE_STATE =
            "notification_queue_state pendingCount={} oldestActiveAgeSeconds={}";
}
