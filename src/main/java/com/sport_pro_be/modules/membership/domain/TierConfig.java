package com.sport_pro_be.modules.membership.domain;

import com.sport_pro_be.modules.auth.enums.UserTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "membership_tier_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private UserTier tier;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal threshold;

    private String description;
}
