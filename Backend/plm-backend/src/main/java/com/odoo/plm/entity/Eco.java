package com.odoo.plm.entity;

import com.odoo.plm.enums.EcoStatus;
import com.odoo.plm.enums.EcoType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ecos", indexes = {
        @Index(name = "idx_eco_product_id", columnList = "product_id"),
        @Index(name = "idx_eco_bom_id", columnList = "bom_id"),
        @Index(name = "idx_eco_created_by", columnList = "created_by"),
        @Index(name = "idx_eco_current_stage_id", columnList = "current_stage_id"),
        @Index(name = "idx_eco_status", columnList = "status"),
        @Index(name = "idx_eco_eco_type", columnList = "eco_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Eco {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "eco_type", nullable = false, length = 20)
    private EcoType ecoType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id")
    private Bom bom;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stage_id", nullable = false)
    private EcoStage currentStage;

    @NotNull
    @Column(name = "version_update", nullable = false)
    @Builder.Default
    private Boolean versionUpdate = false;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EcoStatus status = EcoStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "eco", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EcoApproval> approvals = new ArrayList<>();

    @OneToMany(mappedBy = "eco", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EcoBomChange> bomChanges = new ArrayList<>();

    @OneToMany(mappedBy = "eco", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EcoProductChange> productChanges = new ArrayList<>();

    @OneToMany(mappedBy = "eco", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EcoBomOperationChange> bomOperationChanges = new ArrayList<>();

    @OneToMany(mappedBy = "eco", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EcoAttachment> attachments = new ArrayList<>();
}
