package com.sport_pro_be.modules.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayOsPaymentResponse {
    private Long orderCode;
    private Long amount;
    private String currency;
    private String paymentLinkId;
    private String checkoutUrl;
    private String qrCode;
    private String status;
    private Long expiredAt;
}
