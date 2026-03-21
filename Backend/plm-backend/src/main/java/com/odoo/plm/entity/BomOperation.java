package com.odoo.plm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "bom_operations", indexes = {
        @Index(name = "idx_bom_operation_bom_id", columnList = "bom_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private Bom bom;

    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "work_center", length = 255)
    private String workCenter;

    @PositiveOrZero
    @Column(name = "expected_duration_minutes", precision = 10, scale = 2)
    private BigDecimal expectedDurationMinutes;

    @NotNull
    @Column(name = "sequence", nullable = false)
    @Builder.Default
    private Integer sequence = 1;
}
