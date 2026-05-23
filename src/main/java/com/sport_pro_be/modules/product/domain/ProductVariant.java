package com.sport_pro_be.modules.product.domain;

import com.sport_pro_be.common.AbstractAuditingEntity;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.color.domain.Color;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_product_id", columnList = "product_id"),
        @Index(name = "idx_variant_size", columnList = "size"),
        @Index(name = "idx_variant_color", columnList = "color"),
        @Index(name = "idx_variant_price", columnList = "sale_price"),
        @Index(name = "idx_variant_stock", columnList = "stock_quantity"),
        @Index(name = "idx_variant_filter", columnList = "size, color, sale_price")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(name = "color", nullable = true, length = 50)
    private String colorOld;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_id", nullable = true)
    private Color color;

    @Column(name = "original_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "sale_price", precision = 15, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
}


