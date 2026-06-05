package com.sport_pro_be.modules.custom_design.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "custom_designs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomDesign extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "printing_material_id", nullable = false)
    private PrintingMaterial printingMaterial;

    @Column(name = "design_image_url", nullable = false, columnDefinition = "TEXT")
    private String designImageUrl;

    @Column(name = "back_design_image_url", columnDefinition = "TEXT")
    private String backDesignImageUrl;

    @Column(name = "design_metadata", columnDefinition = "TEXT")
    private String designMetadata;

    @Column(name = "num_text_lines", nullable = false)
    private Integer numTextLines;

    @Column(name = "num_images", nullable = false)
    private Integer numImages;

    @Column(name = "total_printing_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrintingPrice;
}
