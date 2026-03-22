package com.odoo.plm.service;

import com.odoo.plm.dto.request.eco.*;
import com.odoo.plm.dto.response.eco.*;
import com.odoo.plm.entity.*;
import com.odoo.plm.enums.*;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.*;
import com.odoo.plm.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EcoService {

    private final EcoRepository ecoRepository;
    private final EcoApprovalRepository approvalRepository;
    private final EcoBomChangeRepository bomChangeRepository;
    private final EcoProductChangeRepository productChangeRepository;
    private final ProductRepository productRepository;
    private final BomRepository bomRepository;
    private final BomComponentRepository bomComponentRepository;
    private final ProductService productService;
    private final BomService bomService;
    private final EcoStageService stageService;
    private final EcoApprovalRuleService approvalRuleService;
    private final BomOperationRepository bomOperationRepository;
    private final EcoBomOperationChangeRepository bomOperationChangeRepository;
    private final EcoAttachmentRepository ecoAttachmentRepository;
    private final ProductAttachmentRepository productAttachmentRepository;
    private final BomAttachmentRepository bomAttachmentRepository;
    private final FileService fileService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new ECO (Engineering/Admin only)
     */
    @Transactional
    public EcoResponse createEco(CreateEcoRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        if (!securityUtils.canCreateOrModify()) {
            throw new UnauthorizedException("Only Engineering users or Admins can create ECOs");
        }

        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("ECO can only be created for ACTIVE products");
        }

        // Validate BOM for BOM type ECOs
        Bom bom = null;
        if (request.getEcoType() == EcoType.BOM) {
            if (request.getBomId() == null) {
                throw new BadRequestException("BOM ID is required for BOM type ECOs");
            }
            bom = bomRepository.findById(request.getBomId())
                    .orElseThrow(() -> new ResourceNotFoundException("BOM not found: " + request.getBomId()));

            if (bom.getStatus() != BomStatus.ACTIVE) {
                throw new BadRequestException("ECO can only be created for ACTIVE BOMs");
            }

            if (!bom.getProduct().getId().equals(product.getId())) {
                throw new BadRequestException("BOM does not belong to the selected product");
            }
        }

        // Get first stage
        EcoStage firstStage = stageService.getFirstStage(request.getProductId());

        // Create ECO
        Eco eco = Eco.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .ecoType(request.getEcoType())
                .product(product)
                .bom(bom)
                .versionUpdate(request.getVersionUpdate())
                .currentStage(firstStage)
                .status(EcoStatus.DRAFT)
                .createdBy(currentUser)
                .build();

        eco = ecoRepository.save(eco);

        auditService.logAction(AuditService.ECO_CREATED, eco, "ECO: " + eco.getTitle(),
                null, "Type: " + eco.getEcoType() + ", Product: " + product.getName());

        log.info("ECO created: {} by user: {}", eco.getTitle(), currentUser.getLoginId());

        return mapToResponse(eco);
    }

    /**
     * Update ECO (only in DRAFT status)
     */
    @Transactional
    public EcoResponse updateEco(UUID ecoId, UpdateEcoRequest request) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT ECOs can be updated");
        }

        String oldValues = "Title: " + eco.getTitle() + ", VersionUpdate: " + eco.getVersionUpdate();

        if (request.getTitle() != null) {
            eco.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            eco.setDescription(request.getDescription());
        }
        if (request.getVersionUpdate() != null) {
            eco.setVersionUpdate(request.getVersionUpdate());
        }
        if (request.getEffectiveDate() != null) {
            eco.setEffectiveDate(request.getEffectiveDate());
        }

        eco = ecoRepository.save(eco);

        auditService.logAction(AuditService.ECO_UPDATED, eco, "ECO: " + eco.getTitle(),
                oldValues, "Updated");

        return mapToResponse(eco);
    }

    /**
     * Delete an ECO (only in DRAFT status)
     */
    @Transactional
    public void deleteEco(UUID ecoId) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT ECOs can be deleted");
        }

        bomChangeRepository.deleteAll(bomChangeRepository.findByEcoId(ecoId));
        productChangeRepository.deleteAll(productChangeRepository.findByEcoId(ecoId));
        approvalRepository.deleteAll(approvalRepository.findByEcoIdOrderByApprovedAtDesc(ecoId));

        ecoRepository.delete(eco);
        log.info("ECO deleted: {}", eco.getTitle());
    }

    /**
     * Add BOM change to ECO
     */
    @Transactional
    public EcoResponse addBomChange(UUID ecoId, EcoBomChangeRequest request) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Changes can only be added to DRAFT ECOs");
        }

        if (eco.getEcoType() != EcoType.BOM) {
            throw new BadRequestException("BOM changes can only be added to BOM type ECOs");
        }

        EcoBomChange change = new EcoBomChange();
        change.setEco(eco);
        change.setChangeType(request.getChangeType());

        switch (request.getChangeType()) {
            case MODIFIED:
            case REMOVED:
                if (request.getBomComponentId() == null) {
                    throw new BadRequestException("BOM component ID is required for MODIFIED/REMOVED changes");
                }
                BomComponent existingComp = bomComponentRepository.findById(request.getBomComponentId())
                        .orElseThrow(() -> new ResourceNotFoundException("BOM component not found"));

                change.setBomComponent(existingComp);
                change.setOldQuantity(existingComp.getQuantity());
                change.setUnit(existingComp.getUnit());

                if (request.getChangeType() == ChangeType.MODIFIED) {
                    if (request.getNewQuantity() == null) {
                        throw new BadRequestException("New quantity is required for MODIFIED changes");
                    }
                    change.setNewQuantity(request.getNewQuantity());
                }
                break;

            case ADDED:
                if (request.getComponentProductId() == null) {
                    throw new BadRequestException("Component product ID is required for ADDED changes");
                }
                if (request.getNewQuantity() == null || request.getUnit() == null) {
                    throw new BadRequestException("New quantity and unit are required for ADDED changes");
                }

                Product componentProduct = productRepository.findById(request.getComponentProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Component product not found"));

                change.setNewComponentProduct(componentProduct);
                change.setNewQuantity(request.getNewQuantity());
                change.setUnit(request.getUnit());
                break;
        }

        bomChangeRepository.save(change);

        log.info("BOM change added to ECO {}: {} - {}", eco.getTitle(), request.getChangeType(),
                request.getBomComponentId() != null ? request.getBomComponentId() : request.getComponentProductId());

        return mapToResponse(eco);
    }

    /**
     * Add BOM operation change to ECO
     */
    @Transactional
    public EcoResponse addBomOperationChange(UUID ecoId, EcoBomOperationChangeRequest request) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Changes can only be added to DRAFT ECOs");
        }

        if (eco.getEcoType() != EcoType.BOM) {
            throw new BadRequestException("BOM operations changes can only be added to BOM type ECOs");
        }

        EcoBomOperationChange change = new EcoBomOperationChange();
        change.setEco(eco);
        change.setChangeType(request.getChangeType());
        change.setOperationName(request.getOperationName());
        change.setWorkCenter(request.getWorkCenter());
        change.setExpectedDurationMinutes(request.getExpectedDurationMinutes());
        change.setSequence(request.getSequence() != null ? request.getSequence() : 1);

        switch (request.getChangeType()) {
            case MODIFIED:
            case REMOVED:
                if (request.getBomOperationId() == null) {
                    throw new BadRequestException("BOM Operation ID is required for MODIFIED/REMOVED changes");
                }
                BomOperation existingOp = bomOperationRepository.findById(request.getBomOperationId())
                        .orElseThrow(() -> new ResourceNotFoundException("BOM operation not found"));
                change.setBomOperation(existingOp);
                break;
            case ADDED:
                break;
        }

        bomOperationChangeRepository.save(change);

        log.info("BOM operation change added to ECO {}: {} - {}", eco.getTitle(), request.getChangeType(), request.getOperationName());

        return mapToResponse(eco);
    }

    /**
     * Add product change to ECO
     */
    @Transactional
    public EcoResponse addProductChange(UUID ecoId, EcoProductChangeRequest request) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Changes can only be added to DRAFT ECOs");
        }

        if (eco.getEcoType() != EcoType.PRODUCT) {
            throw new BadRequestException("Product changes can only be added to PRODUCT type ECOs");
        }

        // Check if change for this field already exists
        if (productChangeRepository.existsByEcoIdAndFieldName(ecoId, request.getFieldName())) {
            throw new BadRequestException("A change for field '" + request.getFieldName() + "' already exists");
        }

        // Get current value
        String oldValue = getCurrentProductFieldValue(eco.getProduct(), request.getFieldName());

        EcoProductChange change = EcoProductChange.builder()
                .eco(eco)
                .fieldName(request.getFieldName())
                .oldValue(oldValue)
                .newValue(request.getNewValue())
                .build();

        productChangeRepository.save(change);

        log.info("Product change added to ECO {}: {} = {}", eco.getTitle(), request.getFieldName(), request.getNewValue());

        return mapToResponse(eco);
    }

    /**
     * Add attachment to ECO
     */
    @Transactional
    public EcoResponse addAttachment(UUID ecoId, org.springframework.web.multipart.MultipartFile file) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Attachments can only be added to DRAFT ECOs");
        }

        try {
            com.odoo.plm.dto.response.FileResponse fileResponse = fileService.uploadFile(file);
            EcoAttachment attachment = new EcoAttachment();
            attachment.setEco(eco);
            attachment.setFileName(fileResponse.getOriginalFileName());
            attachment.setFileType(fileResponse.getFileType());
            attachment.setFileSize(fileResponse.getFileSize());
            attachment.setFileUrl(fileResponse.getFileUrl());

            ecoAttachmentRepository.save(attachment);

            log.info("Attachment added to ECO {}: {}", eco.getTitle(), attachment.getFileName());
        } catch (java.io.IOException e) {
            throw new BadRequestException("Failed to upload attachment: " + e.getMessage());
        }

        return mapToResponse(eco);
    }

    /**
     * Remove BOM operation change from ECO
     */
    @Transactional
    public EcoResponse removeBomOperationChange(UUID ecoId, UUID changeId) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Changes can only be removed from DRAFT ECOs");
        }

        EcoBomOperationChange change = bomOperationChangeRepository.findById(changeId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM operation change not found"));

        if (!change.getEco().getId().equals(ecoId)) {
            throw new BadRequestException("BOM operation change does not belong to this ECO");
        }

        bomOperationChangeRepository.delete(change);
        log.info("BOM operation change removed from ECO {}: {}", eco.getTitle(), changeId);

        return mapToResponse(eco);
    }

    /**
     * Remove attachment from ECO
     */
    @Transactional
    public EcoResponse removeAttachment(UUID ecoId, UUID attachmentId) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Attachments can only be removed from DRAFT ECOs");
        }

        EcoAttachment attachment = ecoAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        if (!attachment.getEco().getId().equals(ecoId)) {
            throw new BadRequestException("Attachment does not belong to this ECO");
        }

        ecoAttachmentRepository.delete(attachment);
        log.info("Attachment removed from ECO {}: {}", eco.getTitle(), attachmentId);

        return mapToResponse(eco);
    }

    /**
     * Remove a change from ECO
     */
    @Transactional
    public EcoResponse removeChange(UUID ecoId, UUID changeId) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Changes can only be removed from DRAFT ECOs");
        }

        // Try to find and delete BOM change
        bomChangeRepository.findById(changeId).ifPresent(bomChangeRepository::delete);

        // Try to find and delete Product change
        productChangeRepository.findById(changeId).ifPresent(productChangeRepository::delete);

        return mapToResponse(eco);
    }

    /**
     * Submit ECO for approval (moves from DRAFT to IN_PROGRESS)
     */
    @Transactional
    public EcoResponse submitForApproval(UUID ecoId) {
        Eco eco = getEcoAndValidateAccess(ecoId, true);

        if (eco.getStatus() != EcoStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT ECOs can be submitted for approval");
        }

        // Validate changes exist
        int bomChangeCount = bomChangeRepository.countByEcoId(ecoId);
        int productChangeCount = productChangeRepository.countByEcoId(ecoId);

        if (bomChangeCount == 0 && productChangeCount == 0) {
            throw new BadRequestException("ECO must have at least one change before submission");
        }

        eco.setStatus(EcoStatus.IN_PROGRESS);
        eco = ecoRepository.save(eco);

        auditService.logAction(AuditService.ECO_SUBMITTED, eco, "ECO: " + eco.getTitle(),
                "DRAFT", "IN_PROGRESS");

        log.info("ECO submitted for approval: {}", eco.getTitle());

        return mapToResponse(eco);
    }

    /**
     * Approve or reject ECO (Approver/Admin only)
     */
    @Transactional
    public EcoResponse approveEco(UUID ecoId, ApproveEcoRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new ResourceNotFoundException("ECO not found: " + ecoId));

        // Cannot approve own ECO
        if (eco.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You cannot approve your own ECO");
        }

        // Must be approver or admin
        if (!securityUtils.canApprove()) {
            throw new UnauthorizedException("Only Approvers or Admins can approve ECOs");
        }

        // Check if user is assigned as approver for this stage
        boolean isAssignedApprover = approvalRuleService.isApproverForStage(
                eco.getCurrentStage().getId(), currentUser.getId());
        boolean isAdmin = securityUtils.isAdmin();

        if (!isAssignedApprover && !isAdmin) {
            throw new UnauthorizedException("You are not assigned as an approver for this stage");
        }

        if (eco.getStatus() != EcoStatus.IN_PROGRESS) {
            throw new BadRequestException("Only IN_PROGRESS ECOs can be approved/rejected");
        }

        // Check if already approved by this user
        if (approvalRepository.existsByEcoIdAndApproverUserId(ecoId, currentUser.getId())) {
            throw new BadRequestException("You have already submitted a decision for this ECO");
        }

        // Record approval
        EcoApproval approval = EcoApproval.builder()
                .eco(eco)
                .approverUser(currentUser)
                .stage(eco.getCurrentStage())
                .decision(request.getDecision())
                .comments(request.getComments())
                .approvedAt(LocalDateTime.now())
                .build();

        approvalRepository.save(approval);

        if (request.getDecision() == ApprovalDecision.APPROVED) {
            return handleApproval(eco, currentUser);
        } else {
            return handleRejection(eco, currentUser, request.getComments());
        }
    }

    /**
     * Get ECO by ID
     */
    @Transactional(readOnly = true)
    public EcoResponse getEcoById(UUID ecoId) {
        Eco eco = ecoRepository.findByIdWithDetails(ecoId)
                .orElseThrow(() -> new ResourceNotFoundException("ECO not found: " + ecoId));

        User currentUser = securityUtils.getCurrentUser();

        // Operations users cannot see draft/in-progress ECOs
        if (currentUser.getRole() == Role.OPERATIONS_USER &&
                (eco.getStatus() == EcoStatus.DRAFT || eco.getStatus() == EcoStatus.IN_PROGRESS)) {
            throw new UnauthorizedException("You don't have permission to view this ECO");
        }

        return mapToResponse(eco);
    }

    /**
     * Get ECOs created by current user
     */
    @Transactional(readOnly = true)
    public EcoListResponse getMyEcos(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Eco> ecos = ecoRepository.findByCreatedByIdWithDetails(currentUser.getId(), pageable);
        return buildListResponse(ecos);
    }

    /**
     * Get ECOs pending approval for current user
     */
    @Transactional(readOnly = true)
    public EcoListResponse getPendingApprovals(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();

        if (!securityUtils.canApprove()) {
            throw new UnauthorizedException("Only Approvers or Admins can view pending approvals");
        }

        Page<Eco> ecos = ecoRepository.findPendingForApproverWithDetails(currentUser.getId(), pageable);
        return buildListResponse(ecos);
    }

    /**
     * Search ECOs with filters
     */
    @Transactional(readOnly = true)
    public EcoListResponse searchEcos(EcoSearchRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Operations users can only see APPLIED ECOs
        EcoStatus status = request.getStatus();
        if (currentUser.getRole() == Role.OPERATIONS_USER && status == null) {
            status = EcoStatus.APPLIED;
        }

        Page<Eco> ecos = ecoRepository.searchEcosWithDetails(
                request.getTitle(), status, request.getEcoType(), request.getProductId(), pageable);

        return buildListResponse(ecos);
    }

    /**
     * Get all ECOs (for admin/general listing)
     */
    @Transactional(readOnly = true)
    public EcoListResponse getAllEcos(Pageable pageable) {
        Page<Eco> ecos = ecoRepository.findAllWithDetails(pageable);
        return buildListResponse(ecos);
    }

    /**
     * Get comparison view for ECO
     */
    @Transactional(readOnly = true)
    public EcoComparisonResponse getComparison(UUID ecoId) {
        Eco eco = ecoRepository.findByIdWithDetails(ecoId)
                .orElseThrow(() -> new ResourceNotFoundException("ECO not found: " + ecoId));

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.OPERATIONS_USER) {
            throw new UnauthorizedException("Operations users cannot view ECO comparisons");
        }

        return buildComparisonResponse(eco);
    }

    // ==================== Private Helper Methods ====================

    private Eco getEcoAndValidateAccess(UUID ecoId, boolean requireModifyPermission) {
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new ResourceNotFoundException("ECO not found: " + ecoId));

        User currentUser = securityUtils.getCurrentUser();

        if (requireModifyPermission) {
            // Only creator or admin can modify
            if (!eco.getCreatedBy().getId().equals(currentUser.getId()) && !securityUtils.isAdmin()) {
                throw new UnauthorizedException("You don't have permission to modify this ECO");
            }
        }

        return eco;
    }

    private EcoResponse handleApproval(Eco eco, User currentUser) {
        EcoStage currentStage = eco.getCurrentStage();

        // Check if all required approvers have approved
        List<UUID> requiredApproverIds = approvalRuleService.getApproverIdsForStage(currentStage.getId());
        List<EcoApproval> approvals = approvalRepository.findByEcoIdAndStageId(eco.getId(), currentStage.getId());

        int approvedCount = (int) approvals.stream()
                .filter(a -> a.getDecision() == ApprovalDecision.APPROVED)
                .count();

        // Check if we need all required approvers or just any
        boolean hasApprovalRules = approvalRuleService.hasApprovalRules(currentStage.getId());

        if (!hasApprovalRules || approvedCount >= requiredApproverIds.size()) {
            // Move to next stage
            EcoStage nextStage = stageService.getNextStage(currentStage);

            if (nextStage != null) {
                eco.setCurrentStage(nextStage);
                auditService.logAction(AuditService.ECO_STAGE_CHANGED, eco, "ECO: " + eco.getTitle(),
                        currentStage.getName(), nextStage.getName());

                // Check if next stage is final
                if (nextStage.getIsFinal()) {
                    applyEco(eco, currentUser);
                }
            } else if (currentStage.getIsFinal()) {
                // Current stage is final, apply changes
                applyEco(eco, currentUser);
            }

            eco = ecoRepository.save(eco);
        }

        auditService.logAction(AuditService.ECO_APPROVED, eco, "ECO: " + eco.getTitle(),
                "Approved by " + currentUser.getLoginId(), "Stage: " + currentStage.getName());

        log.info("ECO approved: {} by {}", eco.getTitle(), currentUser.getLoginId());

        return mapToResponse(eco);
    }

    private EcoResponse handleRejection(Eco eco, User currentUser, String comments) {
        // Get first stage to return ECO to creator
        EcoStage firstStage = stageService.getFirstStage(eco.getProduct().getId());

        eco.setCurrentStage(firstStage);
        eco.setStatus(EcoStatus.DRAFT);
        eco = ecoRepository.save(eco);

        auditService.logAction(AuditService.ECO_REJECTED, eco, "ECO: " + eco.getTitle(),
                "Rejected by " + currentUser.getLoginId(), "Comments: " + comments);

        log.info("ECO rejected: {} by {}", eco.getTitle(), currentUser.getLoginId());

        return mapToResponse(eco);
    }

    private void applyEco(Eco eco, User actor) {
        eco.setStatus(EcoStatus.APPROVED);
        eco.setEffectiveDate(LocalDate.now());

        if (eco.getEcoType() == EcoType.PRODUCT) {
            Product targetProduct = applyProductChanges(eco, actor);
            applyAttachments(eco, targetProduct, null);
        } else {
            Bom targetBom = applyBomChanges(eco, actor);
            applyAttachments(eco, null, targetBom);
        }

        eco.setStatus(EcoStatus.APPLIED);

        auditService.logAction(AuditService.ECO_APPLIED, eco, "ECO: " + eco.getTitle(),
                "Changes applied", "Effective date: " + eco.getEffectiveDate());

        log.info("ECO applied: {}", eco.getTitle());
    }

    private Product applyProductChanges(Eco eco, User actor) {
        Product originalProduct = eco.getProduct();
        List<EcoProductChange> changes = productChangeRepository.findByEcoId(eco.getId());

        Product targetProduct;
        if (eco.getVersionUpdate()) {
            // Create new version
            targetProduct = productService.createNewVersion(originalProduct, actor);
        } else {
            targetProduct = originalProduct;
        }

        // Apply changes
        for (EcoProductChange change : changes) {
            applyProductFieldChange(targetProduct, change.getFieldName(), change.getNewValue());
        }

        return productRepository.save(targetProduct);
    }

    private Bom applyBomChanges(Eco eco, User actor) {
        Bom originalBom = eco.getBom();
        List<EcoBomChange> changes = bomChangeRepository.findByEcoId(eco.getId());
        List<EcoBomOperationChange> operationChanges = bomOperationChangeRepository.findByEcoIdWithDetails(eco.getId());

        Bom targetBom;
        if (eco.getVersionUpdate()) {
            // Create new version
            targetBom = bomService.createNewVersion(originalBom, actor);
        } else {
            targetBom = originalBom;
        }

        // Apply component changes
        for (EcoBomChange change : changes) {
            switch (change.getChangeType()) {
                case MODIFIED:
                    // Find corresponding component in new BOM and update
                    List<BomComponent> components = bomComponentRepository.findByBomId(targetBom.getId());
                    for (BomComponent comp : components) {
                        if (comp.getComponentProduct().getId().equals(
                                change.getBomComponent().getComponentProduct().getId())) {
                            comp.setQuantity(change.getNewQuantity());
                            bomComponentRepository.save(comp);
                            break;
                        }
                    }
                    break;

                case ADDED:
                    BomComponent newComp = BomComponent.builder()
                            .bom(targetBom)
                            .componentProduct(change.getNewComponentProduct())
                            .quantity(change.getNewQuantity())
                            .unit(change.getUnit())
                            .build();
                    bomComponentRepository.save(newComp);
                    break;

                case REMOVED:
                    List<BomComponent> comps = bomComponentRepository.findByBomId(targetBom.getId());
                    for (BomComponent comp : comps) {
                        if (comp.getComponentProduct().getId().equals(
                                change.getBomComponent().getComponentProduct().getId())) {
                            bomComponentRepository.delete(comp);
                            break;
                        }
                    }
                    break;
            }
        }

        // Apply operation routing changes
        for (EcoBomOperationChange opChange : operationChanges) {
            switch (opChange.getChangeType()) {
                case MODIFIED:
                    List<BomOperation> ops = bomOperationRepository.findByBomId(targetBom.getId());
                    for (BomOperation op : ops) {
                        if (opChange.getBomOperation() != null && op.getId().equals(opChange.getBomOperation().getId())) {
                            op.setName(opChange.getOperationName());
                            op.setWorkCenter(opChange.getWorkCenter());
                            op.setExpectedDurationMinutes(new java.math.BigDecimal(opChange.getExpectedDurationMinutes()));
                            op.setSequence(opChange.getSequence());
                            bomOperationRepository.save(op);
                            break;
                        } else if (opChange.getBomOperation() == null && op.getName().equals(opChange.getOperationName())) {
                            // Best effort match
                            op.setWorkCenter(opChange.getWorkCenter());
                            op.setExpectedDurationMinutes(new java.math.BigDecimal(opChange.getExpectedDurationMinutes()));
                            op.setSequence(opChange.getSequence());
                            bomOperationRepository.save(op);
                            break;
                        }
                    }
                    break;
                case ADDED:
                    BomOperation newOp = BomOperation.builder()
                            .bom(targetBom)
                            .name(opChange.getOperationName())
                            .workCenter(opChange.getWorkCenter())
                            .expectedDurationMinutes(new java.math.BigDecimal(opChange.getExpectedDurationMinutes()))
                            .sequence(opChange.getSequence())
                            .build();
                    bomOperationRepository.save(newOp);
                    break;
                case REMOVED:
                    List<BomOperation> existingOps = bomOperationRepository.findByBomId(targetBom.getId());
                    for (BomOperation op : existingOps) {
                        if (opChange.getBomOperation() != null && op.getId().equals(opChange.getBomOperation().getId())) {
                            bomOperationRepository.delete(op);
                            break;
                        } else if (opChange.getBomOperation() == null && op.getName().equals(opChange.getOperationName())) {
                            bomOperationRepository.delete(op);
                            break;
                        }
                    }
                    break;
            }
        }

        return targetBom;
    }

    private void applyAttachments(Eco eco, Product targetProduct, Bom targetBom) {
        List<EcoAttachment> ecoAttachments = ecoAttachmentRepository.findByEcoId(eco.getId());
        
        for (EcoAttachment ecoAtt : ecoAttachments) {
            if (targetProduct != null) {
                ProductAttachment productAttachment = ProductAttachment.builder()
                        .product(targetProduct)
                        .fileName(ecoAtt.getFileName())
                        .fileType(ecoAtt.getFileType())
                        .fileUrl(ecoAtt.getFileUrl())
                        .build();
                productAttachmentRepository.save(productAttachment);
            } else if (targetBom != null) {
                BomAttachment bomAttachment = BomAttachment.builder()
                        .bom(targetBom)
                        .fileName(ecoAtt.getFileName())
                        .fileType(ecoAtt.getFileType())
                        .fileUrl(ecoAtt.getFileUrl())
                        .build();
                bomAttachmentRepository.save(bomAttachment);
            }
        }
    }

    private void applyProductFieldChange(Product product, String fieldName, String newValue) {
        switch (fieldName.toLowerCase()) {
            case "name":
                product.setName(newValue);
                break;
            case "sale_price":
            case "saleprice":
                product.setSalePrice(new BigDecimal(newValue));
                break;
            case "cost_price":
            case "costprice":
                product.setCostPrice(new BigDecimal(newValue));
                break;
            case "description":
                product.setDescription(newValue);
                break;
            default:
                log.warn("Unknown product field: {}", fieldName);
        }
    }

    private String getCurrentProductFieldValue(Product product, String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "name" -> product.getName() != null ? product.getName() : "";
            case "sale_price", "saleprice" -> product.getSalePrice() != null ? product.getSalePrice().toString() : "0.0";
            case "cost_price", "costprice" -> product.getCostPrice() != null ? product.getCostPrice().toString() : "0.0";
            case "description" -> product.getDescription() != null ? product.getDescription() : "";
            default -> "";
        };
    }

    private EcoListResponse buildListResponse(Page<Eco> ecos) {
        List<EcoResponse> responses = ecos.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return EcoListResponse.builder()
                .ecos(responses)
                .totalElements(ecos.getTotalElements())
                .totalPages(ecos.getTotalPages())
                .currentPage(ecos.getNumber())
                .pageSize(ecos.getSize())
                .hasNext(ecos.hasNext())
                .hasPrevious(ecos.hasPrevious())
                .build();
    }

    private EcoResponse mapToResponse(Eco eco) {
        // Use JOIN FETCH queries to avoid lazy loading exceptions
        List<EcoBomChange> bomChanges = bomChangeRepository.findByEcoIdWithDetails(eco.getId());
        List<EcoProductChange> productChanges = productChangeRepository.findByEcoId(eco.getId());
        List<EcoApproval> approvals = approvalRepository.findByEcoIdWithDetails(eco.getId());

        // Get final stage safely
        EcoStage finalStage = null;
        if (eco.getProduct() != null) {
            finalStage = stageService.findFinalStage(eco.getProduct().getId()).orElse(null);
        }

        // Extract product info (already eagerly loaded via JOIN FETCH)
        String productName = "Unknown Product";
        Integer productVersion = null;
        UUID productId = null;
        if (eco.getProduct() != null) {
            productName = eco.getProduct().getName();
            productVersion = eco.getProduct().getVersion();
            productId = eco.getProduct().getId();
        }

        // Extract BOM info (already eagerly loaded via JOIN FETCH)
        String bomReference = null;
        Integer bomVersion = null;
        UUID bomId = null;
        if (eco.getBom() != null) {
            bomReference = eco.getBom().getReference();
            bomVersion = eco.getBom().getVersion();
            bomId = eco.getBom().getId();
        }

        // Extract stage info (already eagerly loaded via JOIN FETCH)
        String currentStageName = "Unknown Stage";
        Integer currentStageSequence = null;
        UUID currentStageId = null;
        if (eco.getCurrentStage() != null) {
            currentStageName = eco.getCurrentStage().getName();
            currentStageSequence = eco.getCurrentStage().getSequence();
            currentStageId = eco.getCurrentStage().getId();
        }

        // Extract creator info (already eagerly loaded via JOIN FETCH)
        String createdByName = "Unknown User";
        UUID createdById = null;
        if (eco.getCreatedBy() != null) {
            createdByName = eco.getCreatedBy().getFirstName() + " " + eco.getCreatedBy().getLastName();
            createdById = eco.getCreatedBy().getId();
        }

        return EcoResponse.builder()
                .id(eco.getId())
                .title(eco.getTitle())
                .description(eco.getDescription())
                .ecoType(eco.getEcoType())
                .status(eco.getStatus())
                .versionUpdate(eco.getVersionUpdate())
                .effectiveDate(eco.getEffectiveDate())
                .productId(productId)
                .productName(productName)
                .productVersion(productVersion)
                .bomId(bomId)
                .bomReference(bomReference)
                .bomVersion(bomVersion)
                .currentStageId(currentStageId)
                .currentStageName(currentStageName)
                .currentStageSequence(currentStageSequence)
                .isAtFinalStage(finalStage != null && currentStageId != null && currentStageId.equals(finalStage.getId()))
                .bomChanges(bomChanges.stream().map(this::mapBomChangeToResponse).toList())
                .productChanges(productChanges.stream().map(this::mapProductChangeToResponse).toList())
                .bomOperationChanges(eco.getBomOperationChanges() != null ? eco.getBomOperationChanges().stream().map(this::mapBomOperationChangeToResponse).toList() : List.of())
                .attachments(eco.getAttachments() != null ? eco.getAttachments().stream().map(this::mapEcoAttachmentToResponse).toList() : List.of())
                .approvals(approvals.stream().map(this::mapApprovalToResponse).toList())
                .createdById(createdById)
                .createdByName(createdByName)
                .createdAt(eco.getCreatedAt())
                .updatedAt(eco.getUpdatedAt())
                .build();
    }

    private EcoBomOperationChangeResponse mapBomOperationChangeToResponse(EcoBomOperationChange change) {
        return EcoBomOperationChangeResponse.builder()
                .id(change.getId())
                .changeType(change.getChangeType())
                .bomOperationId(change.getBomOperation() != null ? change.getBomOperation().getId() : null)
                .operationName(change.getOperationName())
                .workCenter(change.getWorkCenter())
                .expectedDurationMinutes(change.getExpectedDurationMinutes())
                .sequence(change.getSequence())
                .build();
    }

    private EcoAttachmentResponse mapEcoAttachmentToResponse(EcoAttachment attachment) {
        return EcoAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileUrl(attachment.getFileUrl())
                .fileSize(attachment.getFileSize())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
    private EcoBomChangeResponse mapBomChangeToResponse(EcoBomChange change) {
        // Data already eagerly loaded via JOIN FETCH in findByEcoIdWithDetails
        String componentName = "Unknown Component";
        UUID bomComponentId = null;
        if (change.getBomComponent() != null) {
            bomComponentId = change.getBomComponent().getId();
            if (change.getBomComponent().getComponentProduct() != null) {
                componentName = change.getBomComponent().getComponentProduct().getName();
            }
        } else if (change.getNewComponentProduct() != null) {
            componentName = change.getNewComponentProduct().getName();
        }

        return EcoBomChangeResponse.builder()
                .id(change.getId())
                .bomComponentId(bomComponentId)
                .componentName(componentName)
                .oldQuantity(change.getOldQuantity())
                .newQuantity(change.getNewQuantity())
                .unit(change.getUnit())
                .changeType(change.getChangeType())
                .build();
    }

    private EcoProductChangeResponse mapProductChangeToResponse(EcoProductChange change) {
        return EcoProductChangeResponse.builder()
                .id(change.getId())
                .fieldName(change.getFieldName())
                .oldValue(change.getOldValue())
                .newValue(change.getNewValue())
                .build();
    }

    private EcoApprovalResponse mapApprovalToResponse(EcoApproval approval) {
        // Data already eagerly loaded via JOIN FETCH in findByEcoIdWithDetails
        UUID approverId = null;
        String approverName = "Unknown";
        UUID stageId = null;
        String stageName = "Unknown";

        if (approval.getApproverUser() != null) {
            approverId = approval.getApproverUser().getId();
            approverName = approval.getApproverUser().getFirstName() + " " + approval.getApproverUser().getLastName();
        }

        if (approval.getStage() != null) {
            stageId = approval.getStage().getId();
            stageName = approval.getStage().getName();
        }

        return EcoApprovalResponse.builder()
                .id(approval.getId())
                .approverId(approverId)
                .approverName(approverName)
                .stageId(stageId)
                .stageName(stageName)
                .decision(approval.getDecision())
                .comments(approval.getComments())
                .approvedAt(approval.getApprovedAt())
                .build();
    }

    private EcoComparisonResponse buildComparisonResponse(Eco eco) {
        if (eco.getEcoType() == EcoType.PRODUCT) {
            List<EcoComparisonResponse.ChangeItem> changeItems = new ArrayList<>();
            List<EcoProductChange> changes = productChangeRepository.findByEcoId(eco.getId());
            for (EcoProductChange change : changes) {
                changeItems.add(EcoComparisonResponse.ChangeItem.builder()
                        .field(change.getFieldName())
                        .oldValue(change.getOldValue())
                        .newValue(change.getNewValue())
                        .changeType("MODIFIED")
                        .color("black")
                        .build());
            }
            return EcoComparisonResponse.builder()
                    .ecoId(eco.getId())
                    .ecoTitle(eco.getTitle())
                    .type("Product")
                    .changes(changeItems)
                    .build();
        } else {
            List<EcoComparisonResponse.ComponentComparison> componentComparisons = new ArrayList<>();
            List<EcoComparisonResponse.OperationComparison> operationComparisons = new ArrayList<>();
            
            BigDecimal totalVersion1Parts = BigDecimal.ZERO;
            BigDecimal totalVersion2Parts = BigDecimal.ZERO;
            Integer totalVersion1Time = 0;
            Integer totalVersion2Time = 0;

            if (eco.getBom() != null) {
                // 1. Process Components
                List<BomComponent> originalComponents = bomComponentRepository.findByBomId(eco.getBom().getId());
                List<EcoBomChange> bomChanges = bomChangeRepository.findByEcoIdWithDetails(eco.getId());

                for (BomComponent comp : originalComponents) {
                    EcoComparisonResponse.ComponentComparison cc = EcoComparisonResponse.ComponentComparison.builder()
                            .id(comp.getId())
                            .name(comp.getComponentProduct().getName())
                            .version1Qty(comp.getQuantity())
                            .version2Qty(comp.getQuantity()) // default unchanged
                            .unit(comp.getUnit())
                            .changeType("UNCHANGED")
                            .build();

                    // Check for changes to this component
                    for (EcoBomChange change : bomChanges) {
                        if (change.getBomComponent() != null && change.getBomComponent().getId().equals(comp.getId())) {
                            if (change.getChangeType() == ChangeType.MODIFIED) {
                                cc.setVersion2Qty(change.getNewQuantity());
                                cc.setChangeType("UPDATE");
                            } else if (change.getChangeType() == ChangeType.REMOVED) {
                                cc.setVersion2Qty(BigDecimal.ZERO);
                                cc.setChangeType("REMOVE");
                            }
                            break;
                        }
                    }
                    componentComparisons.add(cc);
                    totalVersion1Parts = totalVersion1Parts.add(cc.getVersion1Qty() != null ? cc.getVersion1Qty() : BigDecimal.ZERO);
                    totalVersion2Parts = totalVersion2Parts.add(cc.getVersion2Qty() != null ? cc.getVersion2Qty() : BigDecimal.ZERO);
                }

                // Add ADDED components
                for (EcoBomChange change : bomChanges) {
                    if (change.getChangeType() == ChangeType.ADDED) {
                        EcoComparisonResponse.ComponentComparison cc = EcoComparisonResponse.ComponentComparison.builder()
                                .id(change.getId()) // use change id as temp id
                                .name(change.getNewComponentProduct().getName())
                                .version1Qty(BigDecimal.ZERO)
                                .version2Qty(change.getNewQuantity())
                                .unit(change.getUnit())
                                .changeType("ADD")
                                .build();
                        componentComparisons.add(cc);
                        totalVersion2Parts = totalVersion2Parts.add(cc.getVersion2Qty() != null ? cc.getVersion2Qty() : BigDecimal.ZERO);
                    }
                }

                // 2. Process Operations
                List<BomOperation> originalOperations = bomOperationRepository.findByBomId(eco.getBom().getId());
                List<EcoBomOperationChange> operationChanges = bomOperationChangeRepository.findByEcoIdWithDetails(eco.getId());

                for (BomOperation op : originalOperations) {
                    EcoComparisonResponse.OperationComparison oc = EcoComparisonResponse.OperationComparison.builder()
                            .id(op.getId())
                            .name(op.getName())
                            .workCenter(op.getWorkCenter())
                            .version1Duration(op.getExpectedDurationMinutes().intValue())
                            .version2Duration(op.getExpectedDurationMinutes().intValue()) // default unchanged
                            .changeType("UNCHANGED")
                            .build();

                    // Check for changes
                    for (EcoBomOperationChange change : operationChanges) {
                        if (change.getBomOperation() != null && change.getBomOperation().getId().equals(op.getId())) {
                            if (change.getChangeType() == ChangeType.MODIFIED) {
                                oc.setVersion2Duration(change.getExpectedDurationMinutes());
                                oc.setChangeType("UPDATE");
                            } else if (change.getChangeType() == ChangeType.REMOVED) {
                                oc.setVersion2Duration(0);
                                oc.setChangeType("REMOVE");
                            }
                            break;
                        }
                    }
                    operationComparisons.add(oc);
                    totalVersion1Time += oc.getVersion1Duration() != null ? oc.getVersion1Duration() : 0;
                    totalVersion2Time += oc.getVersion2Duration() != null ? oc.getVersion2Duration() : 0;
                }

                // Add ADDED operations
                for (EcoBomOperationChange change : operationChanges) {
                    if (change.getChangeType() == ChangeType.ADDED) {
                        EcoComparisonResponse.OperationComparison oc = EcoComparisonResponse.OperationComparison.builder()
                                .id(change.getId())
                                .name(change.getOperationName())
                                .workCenter(change.getWorkCenter())
                                .version1Duration(0)
                                .version2Duration(change.getExpectedDurationMinutes())
                                .changeType("ADD")
                                .build();
                        operationComparisons.add(oc);
                        totalVersion2Time += oc.getVersion2Duration() != null ? oc.getVersion2Duration() : 0;
                    }
                }
            }

            EcoComparisonResponse.ComparisonSummary summary = EcoComparisonResponse.ComparisonSummary.builder()
                    .totalPartsDelta(totalVersion2Parts.subtract(totalVersion1Parts))
                    .productionCycleDelta(totalVersion2Time - totalVersion1Time)
                    .estimatedCostImpact(BigDecimal.ZERO) // Calculating proper impact would require crawling all parts costs
                    .build();

            return EcoComparisonResponse.builder()
                    .ecoId(eco.getId())
                    .ecoTitle(eco.getTitle())
                    .type("BOM")
                    .components(componentComparisons)
                    .operations(operationComparisons)
                    .summary(summary)
                    .build();
        }
    }
}
