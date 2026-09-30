package com.sport_pro_be.modules.payment.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.payment.constant.PaymentMessageConstant;
import com.sport_pro_be.modules.payment.dto.PayOsPaymentResponse;
import com.sport_pro_be.modules.payment.service.PayOsPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/v1/payments/payos")
@RequiredArgsConstructor
public class PayOsPaymentController {

    private final PayOsPaymentService payOsPaymentService;

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PayOsPaymentResponse>> createPaymentLink(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        PayOsPaymentResponse response = payOsPaymentService.createPaymentLink(userId, orderId);
        return ResponseEntity.ok(ApiResponse.of(PaymentMessageConstant.PAYOS_PAYMENT_LINK_CREATED, response));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Webhook webhook) {
        payOsPaymentService.handleWebhook(webhook);
        return ResponseEntity.ok(PaymentMessageConstant.PAYOS_WEBHOOK_RECEIVED);
    }
}
