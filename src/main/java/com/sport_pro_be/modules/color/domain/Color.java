package com.sport_pro_be.modules.color.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colors", indexes = {
        @Index(name = "idx_color_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;
}
