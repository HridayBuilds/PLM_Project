package com.odoo.plm.entity;

import com.odoo.plm.enums.ChangeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "eco_bom_changes", indexes = {
        @Index(name = "idx_eco_bom_change_eco_id", columnList = "eco_id"),
        @Index(name = "idx_eco_bom_change_component_id", columnList = "bom_component_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoBomChange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eco_id", nullable = false)
    private Eco eco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_component_id")
    private BomComponent bomComponent;

    @Column(name = "old_quantity", precision = 15, scale = 4)
    private BigDecimal oldQuantity;

    @Column(name = "new_quantity", precision = 15, scale = 4)
    private BigDecimal newQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "unit", length = 50)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_component_product_id")
    private Product newComponentProduct;
}
