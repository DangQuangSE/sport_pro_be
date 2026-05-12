package com.sport_pro_be.modules.coupon.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import com.sport_pro_be.modules.auth.enums.UserTier;
import com.sport_pro_be.modules.coupon.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserTier requiredTier;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer usageLimit;

    @Builder.Default
    private Integer usedCount = 0;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isDeleted = false;
}
