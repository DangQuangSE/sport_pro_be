package com.sport_pro_be.modules.size.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sport_pro_be.common.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "size_options", indexes = {
        @Index(name = "idx_size_option_name", columnList = "name"),
        @Index(name = "idx_size_option_group", columnList = "size_group_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeOption extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_group_id", nullable = false)
    @JsonBackReference
    private SizeGroup sizeGroup;
}
