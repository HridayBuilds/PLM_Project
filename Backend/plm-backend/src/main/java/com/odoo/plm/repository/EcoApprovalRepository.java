package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoApproval;
import com.odoo.plm.enums.ApprovalDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EcoApprovalRepository extends JpaRepository<EcoApproval, UUID> {

    // Find approvals for an ECO
    List<EcoApproval> findByEcoId(UUID ecoId);

    List<EcoApproval> findByEcoIdOrderByApprovedAtDesc(UUID ecoId);

    // Find approvals by approver
    Page<EcoApproval> findByApproverUserId(UUID approverId, Pageable pageable);

    // Find approval by ECO and approver
    Optional<EcoApproval> findByEcoIdAndApproverUserId(UUID ecoId, UUID approverId);

    // Check if user has approved an ECO
    boolean existsByEcoIdAndApproverUserId(UUID ecoId, UUID approverId);

    // Find approvals by ECO and stage
    List<EcoApproval> findByEcoIdAndStageId(UUID ecoId, UUID stageId);

    // Find approvals by decision
    List<EcoApproval> findByEcoIdAndDecision(UUID ecoId, ApprovalDecision decision);

    // Count approvals for an ECO at a stage
    int countByEcoIdAndStageId(UUID ecoId, UUID stageId);

    // Check if all required approvers have approved for a stage
    @Query("SELECT COUNT(a) FROM EcoApproval a WHERE a.eco.id = :ecoId AND a.stage.id = :stageId AND a.decision = 'APPROVED'")
    int countApprovedByEcoAndStage(@Param("ecoId") UUID ecoId, @Param("stageId") UUID stageId);

    // Get approval history for an ECO
    @Query("SELECT a FROM EcoApproval a WHERE a.eco.id = :ecoId ORDER BY a.approvedAt ASC")
    List<EcoApproval> findApprovalHistory(@Param("ecoId") UUID ecoId);

    // JOIN FETCH to eagerly load approverUser and stage
    @Query("SELECT a FROM EcoApproval a " +
            "LEFT JOIN FETCH a.approverUser " +
            "LEFT JOIN FETCH a.stage " +
            "WHERE a.eco.id = :ecoId ORDER BY a.approvedAt DESC")
    List<EcoApproval> findByEcoIdWithDetails(@Param("ecoId") UUID ecoId);
}
