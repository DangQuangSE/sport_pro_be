package com.sport_pro_be.modules.auth.domain;

import com.sport_pro_be.modules.auth.enums.Role;
import com.sport_pro_be.modules.auth.enums.UserTier;
import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "app_users", indexes = {
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_tier", columnList = "tier")
})
public class User extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    private String firstName;
    private String lastName;
    private String avatar;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false)
    private Integer tokenVersion = 1;

    @Column(name = "total_spending", precision = 15, scale = 2)
    private BigDecimal totalSpending = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserTier tier = UserTier.BRONZE;
}

