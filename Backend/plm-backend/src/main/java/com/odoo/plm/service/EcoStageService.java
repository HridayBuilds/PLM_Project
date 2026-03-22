package com.odoo.plm.service;

import com.odoo.plm.dto.request.admin.CreateEcoStageRequest;
import com.odoo.plm.dto.request.admin.ReorderStagesRequest;
import com.odoo.plm.dto.request.admin.UpdateEcoStageRequest;
import com.odoo.plm.dto.response.eco.EcoStageResponse;
import com.odoo.plm.entity.EcoStage;
import com.odoo.plm.entity.Product;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.DuplicateResourceException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.EcoApprovalRuleRepository;
import com.odoo.plm.repository.EcoRepository;
import com.odoo.plm.repository.EcoStageRepository;
import com.odoo.plm.repository.ProductRepository;
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
    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    @Transactional
    public EcoStageResponse createStage(CreateEcoStageRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can create ECO stages");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        if (stageRepository.existsByProductIdAndName(request.getProductId(), request.getName())) {
            throw new DuplicateResourceException("A stage with name '" + request.getName() + "' already exists for this product");
        }

        if (stageRepository.existsByProductIdAndSequence(request.getProductId(), request.getSequence())) {
            throw new BadRequestException("A stage with sequence " + request.getSequence() + " already exists for this product");
        }

        if (request.getIsFinal()) {
            stageRepository.findByProductIdAndIsFinalTrue(request.getProductId()).ifPresent(existingFinal -> {
                throw new BadRequestException("Stage '" + existingFinal.getName() + "' is already marked as final for this product");
            });
        }

        EcoStage stage = EcoStage.builder()
                .name(request.getName())
                .sequence(request.getSequence())
                .isFinal(request.getIsFinal())
                .product(product)
                .build();

        stage = stageRepository.save(stage);

        auditService.logAction(AuditService.STAGE_CREATED, "Stage: " + stage.getName(),
                null, "Sequence: " + stage.getSequence());

        log.info("ECO stage created: {}", stage.getName());

        return mapToResponse(stage);
    }

    @Transactional
    public EcoStageResponse updateStage(UUID stageId, UpdateEcoStageRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can update ECO stages");
        }

        EcoStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));

        UUID productId = stage.getProduct() != null ? stage.getProduct().getId() : null;
        String oldValues = "Name: " + stage.getName() + ", Sequence: " + stage.getSequence() + ", IsFinal: " + stage.getIsFinal();

        if (request.getName() != null && !request.getName().equals(stage.getName())) {
            if (productId != null && stageRepository.existsByProductIdAndName(productId, request.getName())) {
                throw new DuplicateResourceException("A stage with name '" + request.getName() + "' already exists for this product");
            }
            stage.setName(request.getName());
        }

        if (request.getSequence() != null && !request.getSequence().equals(stage.getSequence())) {
            if (productId != null && stageRepository.existsByProductIdAndSequence(productId, request.getSequence())) {
                throw new BadRequestException("A stage with sequence " + request.getSequence() + " already exists for this product");
            }
            stage.setSequence(request.getSequence());
        }

        if (request.getIsFinal() != null) {
            if (request.getIsFinal() && !stage.getIsFinal() && productId != null) {
                stageRepository.findByProductIdAndIsFinalTrue(productId).ifPresent(existingFinal -> {
                    if (!existingFinal.getId().equals(stageId)) {
                        throw new BadRequestException("Stage '" + existingFinal.getName() + "' is already marked as final for this product");
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

    @Transactional
    public void deleteStage(UUID stageId) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can delete ECO stages");
        }

        EcoStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));

        long ecoCount = ecoRepository.findByCurrentStageId(stageId,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        if (ecoCount > 0) {
            throw new BadRequestException("Cannot delete stage that has ECOs. Move ECOs to another stage first.");
        }

        approvalRuleRepository.deleteByEcoStageId(stageId);
        stageRepository.delete(stage);

        auditService.logAction(AuditService.STAGE_DELETED, "Stage: " + stage.getName(),
                "Deleted", null);

        log.info("ECO stage deleted: {}", stage.getName());
    }

    @Transactional
    public List<EcoStageResponse> reorderStages(ReorderStagesRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can reorder ECO stages");
        }

        UUID productId = null;
        boolean isGlobalStages = false;
        for (ReorderStagesRequest.StageOrder order : request.getStageOrders()) {
            EcoStage stage = stageRepository.findById(order.getStageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + order.getStageId()));

            if (productId == null && stage.getProduct() != null) {
                productId = stage.getProduct().getId();
            } else if (stage.getProduct() == null) {
                isGlobalStages = true;
            }
            stage.setSequence(order.getSequence());
            stageRepository.save(stage);
        }

        log.info("ECO stages reordered");

        if (productId != null) {
            return getStagesByProduct(productId);
        } else if (isGlobalStages) {
            // Return global stages
            return stageRepository.findAllGlobalStagesOrderBySequenceAsc().stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<EcoStageResponse> getStagesByProduct(UUID productId) {
        return stageRepository.findAllByProductIdOrderBySequenceAsc(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EcoStageResponse> getAllStages() {
        return stageRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EcoStage getFirstStage(UUID productId) {
        // First try to find product-specific stages
        return stageRepository.findFirstByProductIdOrderBySequenceAsc(productId)
                // If no product-specific stages, fall back to global stages
                .or(() -> stageRepository.findFirstGlobalStageOrderBySequenceAsc())
                .orElseThrow(() -> new BadRequestException("No ECO stages configured. Admin must create stages first."));
    }

    @Transactional(readOnly = true)
    public EcoStage getFinalStage(UUID productId) {
        // First try to find product-specific final stage
        return stageRepository.findByProductIdAndIsFinalTrue(productId)
                // If no product-specific final stage, fall back to global final stage
                .or(() -> stageRepository.findGlobalFinalStage())
                .orElseThrow(() -> new BadRequestException("No final stage configured. Admin must mark a stage as final."));
    }

    /**
     * Safe version that returns Optional instead of throwing exception.
     * Use this when you don't want transaction to rollback on missing stage.
     */
    public java.util.Optional<EcoStage> findFinalStage(UUID productId) {
        return stageRepository.findByProductIdAndIsFinalTrue(productId)
                .or(() -> stageRepository.findGlobalFinalStage());
    }

    @Transactional(readOnly = true)
    public EcoStage getNextStage(EcoStage currentStage) {
        // Check if current stage belongs to a product
        if (currentStage.getProduct() != null) {
            return stageRepository.findFirstByProductIdAndSequenceGreaterThanOrderBySequenceAsc(
                    currentStage.getProduct().getId(), currentStage.getSequence()).orElse(null);
        }
        // Global stage - find next global stage
        return stageRepository.findNextGlobalStage(currentStage.getSequence()).orElse(null);
    }

    @Transactional(readOnly = true)
    public EcoStage getStageById(UUID stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found: " + stageId));
    }

    private EcoStageResponse mapToResponse(EcoStage stage) {
        int approverCount = approvalRuleRepository.countByEcoStageId(stage.getId());
        List<UUID> requiredApprovers = approvalRuleRepository.findRequiredApproverIdsByStageId(stage.getId());

        UUID productId = null;
        String productName = null;
        if (stage.getProduct() != null) {
            productId = stage.getProduct().getId();
            productName = stage.getProduct().getName();
        }

        return EcoStageResponse.builder()
                .id(stage.getId())
                .name(stage.getName())
                .sequence(stage.getSequence())
                .isFinal(stage.getIsFinal())
                .approverCount(approverCount)
                .requiredApproverCount(requiredApprovers.size())
                .productId(productId)
                .productName(productName)
                .build();
    }
}
