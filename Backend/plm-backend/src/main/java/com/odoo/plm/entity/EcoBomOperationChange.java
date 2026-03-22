package com.odoo.plm.entity;

import com.odoo.plm.enums.ChangeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "eco_bom_operation_changes", indexes = {
        @Index(name = "idx_eco_bom_op_change_eco_id", columnList = "eco_id"),
        @Index(name = "idx_eco_bom_op_change_op_id", columnList = "bom_operation_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoBomOperationChange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eco_id", nullable = false)
    private Eco eco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_operation_id")
    private BomOperation bomOperation; // The original operation if MODIFIED or REMOVED

    @NotBlank
    @Column(name = "operation_name", nullable = false, length = 255)
    private String operationName; // The new or modified name

    @Column(name = "work_center", length = 255)
    private String workCenter; // The new or modified work center

    @NotNull
    @Column(name = "expected_duration_minutes", nullable = false)
    private Integer expectedDurationMinutes;

    @NotNull
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;
}
