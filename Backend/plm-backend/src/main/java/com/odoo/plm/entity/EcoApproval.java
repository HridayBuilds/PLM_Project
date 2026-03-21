package com.odoo.plm.entity;

import com.odoo.plm.enums.ApprovalDecision;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eco_approvals", indexes = {
        @Index(name = "idx_eco_approval_eco_id", columnList = "eco_id"),
        @Index(name = "idx_eco_approval_approver_id", columnList = "approver_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eco_id", nullable = false)
    private Eco eco;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id", nullable = false)
    private User approverUser;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private EcoStage stage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private ApprovalDecision decision;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;
}
