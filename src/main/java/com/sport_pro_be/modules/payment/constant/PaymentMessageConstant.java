package com.sport_pro_be.modules.payment.constant;

public final class PaymentMessageConstant {

    private PaymentMessageConstant() {
    }

    public static final String PAYOS_PAYMENT_LINK_CREATED = "PayOS payment link created successfully";
    public static final String PAYOS_WEBHOOK_RECEIVED = "PayOS webhook received";
    public static final String ORDER_NOT_FOUND = "Order not found or does not belong to the user";
    public static final String PAYOS_ONLY = "This order is not configured for PayOS payment";
    public static final String INVALID_PAYMENT_STATUS = "This order is no longer waiting for payment";
    public static final String INVALID_PAYMENT_AMOUNT = "PayOS payment amount does not match the order amount";
    public static final String PAYOS_NOT_CONFIGURED = "PayOS is not configured";
    public static final String INVALID_PAYOS_WEBHOOK = "Invalid PayOS webhook";
}
