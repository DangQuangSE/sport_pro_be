package com.sport_pro_be.modules.payment.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.order.domain.Order;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.enums.PaymentMethod;
import com.sport_pro_be.modules.order.enums.PricingSnapshotStatus;
import com.sport_pro_be.modules.order.repository.OrderRepository;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import com.sport_pro_be.modules.payment.constant.PaymentMessageConstant;
import com.sport_pro_be.modules.payment.dto.PayOsPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOsPaymentService {

    private final OrderRepository orderRepository;
    private final ITierService tierService;

    @Value("${PAYOS_CLIENT_ID:}")
    private String clientId;

    @Value("${PAYOS_API_KEY:}")
    private String apiKey;

    @Value("${PAYOS_CHECKSUM_KEY:}")
    private String checksumKey;

    @Value("${PAYOS_RETURN_URL:}")
    private String returnUrl;

    @Value("${PAYOS_CANCEL_URL:}")
    private String cancelUrl;

    @Transactional(readOnly = true)
    public PayOsPaymentResponse createPaymentLink(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(PaymentMessageConstant.ORDER_NOT_FOUND));

        if (order.getPaymentMethod() != PaymentMethod.PAYOS) {
            throw new BadRequestException(PaymentMessageConstant.PAYOS_ONLY);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(PaymentMessageConstant.INVALID_PAYMENT_STATUS);
        }
        if (order.getPricingSnapshotStatus() != PricingSnapshotStatus.COMPLETE) {
            throw new BadRequestException("Payment requires a complete pricing snapshot");
        }

        long amount = toPayOsAmount(order.getTotalAmount());
        ensureConfigured();

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(order.getId())
                .amount(amount)
                // Keep this short because payOS limits the description length.
                .description("SPORTPRO " + order.getId())
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .build();

        try {
            CreatePaymentLinkResponse paymentLink = payOs().paymentRequests().create(request);
            return toResponse(paymentLink);
        } catch (Exception ex) {
            log.error("Could not create PayOS link for order {}", orderId, ex);
            throw new BadRequestException("Could not create PayOS payment link");
        }
    }

    @Transactional
    public void handleWebhook(Webhook webhook) {
        ensureCredentialsConfigured();

        final WebhookData data;
        try {
            data = payOs().webhooks().verify(webhook);
        } catch (Exception ex) {
            log.warn("Rejected PayOS webhook: {}", ex.getMessage());
            throw new BadRequestException(PaymentMessageConstant.INVALID_PAYOS_WEBHOOK);
        }

        if (data == null || data.getOrderCode() == null) {
            throw new BadRequestException(PaymentMessageConstant.INVALID_PAYOS_WEBHOOK);
        }

        // A validly signed non-success event is acknowledged without changing
        // the order. payOS will retry only when the endpoint returns an error.
        if (!"00".equals(data.getCode())) {
            log.info("PayOS sent non-success event for order {}: {}", data.getOrderCode(), data.getCode());
            return;
        }

        Order order = orderRepository.findById(data.getOrderCode()).orElse(null);
        if (order == null) {
            log.warn("PayOS webhook references unknown order {}", data.getOrderCode());
            return;
        }
        if (order.getPaymentMethod() != PaymentMethod.PAYOS) {
            log.warn("Ignoring PayOS webhook for non-PayOS order {}", order.getId());
            return;
        }

        long expectedAmount = toPayOsAmount(order.getTotalAmount());
        if (data.getAmount() == null || data.getAmount() != expectedAmount) {
            log.error("PayOS amount mismatch for order {}: expected {}, received {}",
                    order.getId(), expectedAmount, data.getAmount());
            throw new BadRequestException(PaymentMessageConstant.INVALID_PAYMENT_AMOUNT);
        }

        // Webhooks are retried by payOS. The state transition and loyalty credit
        // are idempotent, so a retry cannot double-count this order.
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            log.warn("Ignoring PayOS webhook for closed order {} with status {}", order.getId(), order.getStatus());
            return;
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        if (!order.isLoyaltyCredited()) {
            tierService.creditSpending(order.getUser(), order.getTotalAmount());
            order.setLoyaltyCredited(true);
        }

        orderRepository.save(order);
        log.info("Order {} marked {} from PayOS webhook and loyalty credit applied: {}",
                order.getId(), order.getStatus(), order.isLoyaltyCredited());
    }

    private PayOsPaymentResponse toResponse(CreatePaymentLinkResponse paymentLink) {
        return PayOsPaymentResponse.builder()
                .orderCode(paymentLink.getOrderCode())
                .amount(paymentLink.getAmount())
                .currency(paymentLink.getCurrency())
                .paymentLinkId(paymentLink.getPaymentLinkId())
                .checkoutUrl(paymentLink.getCheckoutUrl())
                .qrCode(paymentLink.getQrCode())
                .status(paymentLink.getStatus() == null ? null : paymentLink.getStatus().name())
                .expiredAt(paymentLink.getExpiredAt())
                .build();
    }

    private long toPayOsAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.signum() <= 0) {
            throw new BadRequestException("Order amount must be greater than zero");
        }
        try {
            return totalAmount.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new BadRequestException("Order amount must be a whole VND amount");
        }
    }

    private PayOS payOs() {
        ensureCredentialsConfigured();
        return new PayOS(clientId, apiKey, checksumKey);
    }

    private void ensureConfigured() {
        ensureCredentialsConfigured();
        if (isBlank(returnUrl) || isBlank(cancelUrl)) {
            throw new BadRequestException(PaymentMessageConstant.PAYOS_NOT_CONFIGURED);
        }
    }

    private void ensureCredentialsConfigured() {
        if (isBlank(clientId) || isBlank(apiKey) || isBlank(checksumKey)) {
            throw new BadRequestException(PaymentMessageConstant.PAYOS_NOT_CONFIGURED);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
