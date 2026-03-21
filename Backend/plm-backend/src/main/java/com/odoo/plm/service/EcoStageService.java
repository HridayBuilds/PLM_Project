package com.odoo.plm.service;

import com.odoo.plm.dto.request.admin.CreateEcoStageRequest;
import com.odoo.plm.dto.request.admin.ReorderStagesRequest;
import com.odoo.plm.dto.request.admin.UpdateEcoStageRequest;
import com.odoo.plm.dto.response.eco.EcoStageResponse;
import com.odoo.plm.entity.EcoStage;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.DuplicateResourceException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.EcoApprovalRuleRepository;
import com.odoo.plm.repository.EcoRepository;
import com.odoo.plm.repository.EcoStageRepository;
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
public class EcoStageService {

    private final EcoStageRepository stageRepository;
    private final EcoApprovalRuleRepository approvalRuleRepository;
    private final EcoRepository ecoRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new ECO stage (Admin only)
     */
    @Transactional
    public EcoStageResponse createStage(CreateEcoStageRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can create ECO stages");
        }

        // Check for duplicate name
        if (stageRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("A stage with name '" + request.getName() + "' already exists");
        }

        // Check for duplicate sequence
        if (stageRepository.existsBySequence(request.getSequence())) {
            throw new BadRequestException("A stage with sequence " + request.getSequence() + " already exists");
        }

        // If setting as final, ensure no other stage is final
        if (request.getIsFinal()) {
            stageRepository.findByIsFinalTrue().ifPresent(existingFinal -> {
                throw new BadRequestException("Stage '" + existingFinal.getName() + "' is already marked as final");
            });
        }

        EcoStage stage = EcoStage.builder()
                .name(request.getName())
                .sequence(request.getSequence())
                .isFinal(request.getIsFinal())
                .build();

        stage = stageRepository.save(stage);

        auditService.logAction(AuditService.STAGE_CREATED, "Stage: " + stage.getName(),
                null, "Sequence: " + stage.getSequence());

        log.info("ECO stage created: {}", stage.getName());

        return mapToResponse(stage);
    }

    /**
     * Update an ECO stage (Admin only)
     */
    @Transactional
    public EcoStageResponse updateStage(UUID stageId, UpdateEcoStageRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can update ECO stages");
        }

        EcoStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));

        String oldValues = "Name: " + stage.getName() + ", Sequence: " + stage.getSequence() + ", IsFinal: " + stage.getIsFinal();

        if (request.getName() != null && !request.getName().equals(stage.getName())) {
            if (stageRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("A stage with name '" + request.getName() + "' already exists");
            }
            stage.setName(request.getName());
        }

        if (request.getSequence() != null && !request.getSequence().equals(stage.getSequence())) {
            if (stageRepository.existsBySequence(request.getSequence())) {
                throw new BadRequestException("A stage with sequence " + request.getSequence() + " already exists");
            }
            stage.setSequence(request.getSequence());
        }

        if (request.getIsFinal() != null) {
            if (request.getIsFinal() && !stage.getIsFinal()) {
                // Setting as final - ensure no other is final
                stageRepository.findByIsFinalTrue().ifPresent(existingFinal -> {
                    if (!existingFinal.getId().equals(stageId)) {
                        throw new BadRequestException("Stage '" + existingFinal.getName() + "' is already marked as final");
                    }
                });
            }
            stage.setIsFinal(request.getIsFinal());
        }

        stage = stageRepository.save(stage);

        String newValues = "Name: " + stage.getName() + ", Sequence: " + stage.getSequence() + ", IsFinal: " + stage.getIsFinal();

        auditService.logAction(AuditService.STAGE_UPDATED, "Stage: " + stage.getName(), oldValues, newValues);

        log.info("ECO stage updated: {}", stage.getName());

        return mapToResponse(stage);
    }

    /**
     * Delete an ECO stage (Admin only)
     */
    @Transactional
    public void deleteStage(UUID stageId) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can delete ECO stages");
        }

        EcoStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));

        // Cannot delete if ECOs are using this stage
        long ecoCount = ecoRepository.findByCurrentStageId(stageId,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        if (ecoCount > 0) {
            throw new BadRequestException("Cannot delete stage that has ECOs. Move ECOs to another stage first.");
        }

        // Delete associated approval rules first
        approvalRuleRepository.deleteByEcoStageId(stageId);

        stageRepository.delete(stage);

        auditService.logAction(AuditService.STAGE_DELETED, "Stage: " + stage.getName(),
                "Deleted", null);

        log.info("ECO stage deleted: {}", stage.getName());
    }

    /**
     * Reorder stages (Admin only)
     */
    @Transactional
    public List<EcoStageResponse> reorderStages(ReorderStagesRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can reorder ECO stages");
        }

        for (ReorderStagesRequest.StageOrder order : request.getStageOrders()) {
            EcoStage stage = stageRepository.findById(order.getStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + order.getStageId()));

            stage.setSequence(order.getSequence());
            stageRepository.save(stage);
        }

        log.info("ECO stages reordered");

        return getAllStages();
    }

    /**
     * Get all stages ordered by sequence
     */
    @Transactional(readOnly = true)
    public List<EcoStageResponse> getAllStages() {
        return stageRepository.findAllByOrderBySequenceAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get the first stage (for new ECOs)
     */
    @Transactional(readOnly = true)
    public EcoStage getFirstStage() {
        return stageRepository.findFirstStage()
                .orElseThrow(() -> new BadRequestException("No ECO stages configured. Admin must create stages first."));
    }

    /**
     * Get the final stage
     */
    @Transactional(readOnly = true)
    public EcoStage getFinalStage() {
        return stageRepository.findByIsFinalTrue()
                .orElseThrow(() -> new BadRequestException("No final stage configured. Admin must mark a stage as final."));
    }

    /**
     * Get next stage after current
     */
    @Transactional(readOnly = true)
    public EcoStage getNextStage(EcoStage currentStage) {
        return stageRepository.findNextStage(currentStage.getSequence()).orElse(null);
    }

    /**
     * Get stage by ID
     */
    @Transactional(readOnly = true)
    public EcoStage getStageById(UUID stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));
    }

    private EcoStageResponse mapToResponse(EcoStage stage) {
        int approverCount = approvalRuleRepository.countByEcoStageId(stage.getId());
        List<UUID> requiredApprovers = approvalRuleRepository.findRequiredApproverIdsByStageId(stage.getId());

        return EcoStageResponse.builder()
                .id(stage.getId())
                .name(stage.getName())
                .sequence(stage.getSequence())
                .isFinal(stage.getIsFinal())
                .approverCount(approverCount)
                .requiredApproverCount(requiredApprovers.size())
                .build();
    }
}
