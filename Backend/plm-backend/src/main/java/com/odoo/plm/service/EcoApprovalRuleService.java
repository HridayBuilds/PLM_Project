package com.odoo.plm.service;

import com.odoo.plm.dto.request.admin.CreateApprovalRuleRequest;
import com.odoo.plm.dto.response.admin.ApprovalRuleResponse;
import com.odoo.plm.entity.EcoApprovalRule;
import com.odoo.plm.entity.EcoStage;
import com.odoo.plm.entity.User;
import com.odoo.plm.enums.ApprovalCategory;
import com.odoo.plm.enums.Role;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.DuplicateResourceException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.EcoApprovalRuleRepository;
import com.odoo.plm.repository.EcoStageRepository;
import com.odoo.plm.repository.UserRepository;
import com.odoo.plm.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EcoApprovalRuleService {

    private final EcoApprovalRuleRepository ruleRepository;
    private final EcoStageRepository stageRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    /**
     * Create an approval rule (Admin only)
     */
    @Transactional
    public ApprovalRuleResponse createRule(CreateApprovalRuleRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can create approval rules");
        }

        EcoStage stage = stageRepository.findById(request.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + request.getStageId()));

        User approver = userRepository.findById(request.getApproverUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getApproverUserId()));

        // Validate approver has APPROVER or ADMIN role
        if (approver.getRole() != Role.APPROVER && approver.getRole() != Role.ADMIN) {
            throw new BadRequestException("User must have APPROVER or ADMIN role to be an approver");
        }

        // Check for duplicate rule
        if (ruleRepository.existsByEcoStageIdAndApproverUserId(stage.getId(), approver.getId())) {
            throw new DuplicateResourceException("This user is already an approver for this stage");
        }

        EcoApprovalRule rule = EcoApprovalRule.builder()
                .ecoStage(stage)
                .approverUser(approver)
                .category(request.getCategory())
                .build();

        rule = ruleRepository.save(rule);

        auditService.logAction(AuditService.APPROVAL_RULE_CREATED,
                "Stage: " + stage.getName() + ", Approver: " + approver.getLoginId(),
                null, "Category: " + request.getCategory());

        log.info("Approval rule created: {} for stage {} as {}",
                approver.getLoginId(), stage.getName(), request.getCategory());

        return mapToResponse(rule);
    }

    /**
     * Delete an approval rule (Admin only)
     */
    @Transactional
    public void deleteRule(UUID ruleId) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can delete approval rules");
        }

        EcoApprovalRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval rule not found: " + ruleId));

        ruleRepository.delete(rule);

        auditService.logAction(AuditService.APPROVAL_RULE_DELETED,
                "Stage: " + rule.getEcoStage().getName() + ", Approver: " + rule.getApproverUser().getLoginId(),
                "Deleted", null);

        log.info("Approval rule deleted for {} in stage {}",
                rule.getApproverUser().getLoginId(), rule.getEcoStage().getName());
    }

    /**
     * Get all approval rules
     */
    @Transactional(readOnly = true)
    public List<ApprovalRuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get rules for a specific stage
     */
    @Transactional(readOnly = true)
    public List<ApprovalRuleResponse> getRulesForStage(UUID stageId) {
        return ruleRepository.findByEcoStageId(stageId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get required approvers for a stage
     */
    @Transactional(readOnly = true)
    public List<EcoApprovalRule> getRequiredApproversForStage(UUID stageId) {
        return ruleRepository.findByEcoStageIdAndCategory(stageId, ApprovalCategory.REQUIRED);
    }

    /**
     * Check if user is an approver for a stage
     */
    @Transactional(readOnly = true)
    public boolean isApproverForStage(UUID stageId, UUID userId) {
        return ruleRepository.existsByEcoStageIdAndApproverUserId(stageId, userId);
    }

    /**
     * Check if stage has any approval rules configured
     */
    @Transactional(readOnly = true)
    public boolean hasApprovalRules(UUID stageId) {
        return ruleRepository.existsByEcoStageId(stageId);
    }

    /**
     * Get all approver IDs for a stage
     */
    @Transactional(readOnly = true)
    public List<UUID> getApproverIdsForStage(UUID stageId) {
        return ruleRepository.findApproverIdsByStageId(stageId);
    }

    private ApprovalRuleResponse mapToResponse(EcoApprovalRule rule) {
        return ApprovalRuleResponse.builder()
                .id(rule.getId())
                .stageId(rule.getEcoStage().getId())
                .stageName(rule.getEcoStage().getName())
                .approverUserId(rule.getApproverUser().getId())
                .approverName(rule.getApproverUser().getFirstName() + " " + rule.getApproverUser().getLastName())
                .approverEmail(rule.getApproverUser().getEmail())
                .category(rule.getCategory())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
