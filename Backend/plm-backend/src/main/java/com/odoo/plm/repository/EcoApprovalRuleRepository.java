package com.odoo.plm.repository;

import com.odoo.plm.entity.EcoApprovalRule;
import com.odoo.plm.enums.ApprovalCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EcoApprovalRuleRepository extends JpaRepository<EcoApprovalRule, UUID> {

    // Find rules by stage
    List<EcoApprovalRule> findByEcoStageId(UUID stageId);

    // Find rules by approver
    List<EcoApprovalRule> findByApproverUserId(UUID approverId);

    // Find required approvers for a stage
    List<EcoApprovalRule> findByEcoStageIdAndCategory(UUID stageId, ApprovalCategory category);

    // Check if user is approver for a stage
    boolean existsByEcoStageIdAndApproverUserId(UUID stageId, UUID approverId);

    // Check if stage has any approval rules
    boolean existsByEcoStageId(UUID stageId);

    // Count rules for a stage
    int countByEcoStageId(UUID stageId);

    // Delete rules for a stage
    void deleteByEcoStageId(UUID stageId);

    // Get all approver IDs for a stage
    @Query("SELECT r.approverUser.id FROM EcoApprovalRule r WHERE r.ecoStage.id = :stageId")
    List<UUID> findApproverIdsByStageId(@Param("stageId") UUID stageId);

    // Get required approver IDs for a stage
    @Query("SELECT r.approverUser.id FROM EcoApprovalRule r WHERE r.ecoStage.id = :stageId AND r.category = 'REQUIRED'")
    List<UUID> findRequiredApproverIdsByStageId(@Param("stageId") UUID stageId);
}
