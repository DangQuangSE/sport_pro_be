package com.sport_pro_be.modules.order.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.enums.PaymentMethod;
import com.sport_pro_be.modules.order.enums.PricingSnapshotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_user_id", columnList = "user_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private com.sport_pro_be.modules.coupon.domain.Coupon coupon;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "subtotal_amount", precision = 15, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "printing_amount", precision = 15, scale = 2)
    private BigDecimal printingAmount;

    @Column(name = "tier_discount_amount", precision = 15, scale = 2)
    private BigDecimal tierDiscountAmount;

    @Column(name = "coupon_discount_amount", precision = 15, scale = 2)
    private BigDecimal couponDiscountAmount;

    @Column(name = "shipping_amount", precision = 15, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "applied_tier_code", length = 40)
    private String appliedTierCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pricing_rule_versions", columnDefinition = "jsonb")
    private Map<String, Long> pricingRuleVersions;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_snapshot_status", nullable = false, length = 20)
    @Builder.Default
    private PricingSnapshotStatus pricingSnapshotStatus = PricingSnapshotStatus.LEGACY;

    /**
     * Prevents a successful payment and a later delivery transition from
     * crediting the same order twice.
     */
    @Column(name = "loyalty_credited", nullable = false)
    @Builder.Default
    private boolean loyaltyCredited = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
