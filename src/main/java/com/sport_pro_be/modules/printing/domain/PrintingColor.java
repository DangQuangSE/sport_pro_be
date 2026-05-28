package com.sport_pro_be.modules.printing.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "printing_colors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintingColor extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;

    @Builder.Default
    private Boolean isActive = true;
}
