package com.odoo.plm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eco_stages", indexes = {
        @Index(name = "idx_eco_stage_sequence", columnList = "sequence")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoStage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Positive
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @NotNull
    @Column(name = "is_final", nullable = false)
    @Builder.Default
    private Boolean isFinal = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "ecoStage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EcoApprovalRule> approvalRules = new ArrayList<>();
}
