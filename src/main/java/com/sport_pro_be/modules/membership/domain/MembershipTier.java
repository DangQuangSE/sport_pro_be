package com.sport_pro_be.modules.membership.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "membership_tiers", indexes = {
        @Index(name = "idx_membership_tiers_active_order", columnList = "active, sort_order"),
        @Index(name = "idx_membership_tiers_active_threshold", columnList = "active, threshold")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipTier extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    @Setter(AccessLevel.NONE)
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "display_names", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> displayNames = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "descriptions", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> descriptions = new HashMap<>();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal threshold;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "free_shipping", nullable = false)
    @Builder.Default
    private boolean freeShipping = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> benefits = new HashMap<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

}
