package com.odoo.plm.service;

import com.odoo.plm.dto.request.bom.BomComponentRequest;
import com.odoo.plm.dto.request.bom.BomOperationRequest;
import com.odoo.plm.dto.request.bom.BomSearchRequest;
import com.odoo.plm.dto.request.bom.CreateBomRequest;
import com.odoo.plm.dto.response.FileResponse;
import com.odoo.plm.dto.response.bom.*;
import com.odoo.plm.entity.*;
import com.odoo.plm.enums.BomStatus;
import com.odoo.plm.enums.ProductStatus;
import com.odoo.plm.enums.Role;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.DuplicateResourceException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.*;
import com.odoo.plm.service.storage.FileStorageService;
import com.odoo.plm.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BomService {

    private final BomRepository bomRepository;
    private final BomComponentRepository componentRepository;
    private final BomOperationRepository operationRepository;
    private final ProductRepository productRepository;
    private final BomAttachmentRepository attachmentRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new BOM (Initial creation by Engineering/Admin)
     * BOM starts as DRAFT with version 1
     */
    @Transactional
    public BomResponse createBom(CreateBomRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Only ENGINEERING_USER or ADMIN can create BOMs
        if (!securityUtils.canCreateOrModify()) {
            throw new UnauthorizedException("Only Engineering users or Admins can create BOMs");
        }

        // Validate product exists and is active
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("BOM can only be created for ACTIVE products");
        }

        // Check for duplicate BOM reference
        if (bomRepository.existsByReference(request.getReference())) {
            throw new DuplicateResourceException("A BOM with reference '" + request.getReference() + "' already exists");
        }

        // Create BOM
        Bom bom = Bom.builder()
                .product(product)
                .reference(request.getReference())
                .quantity(request.getQuantity())
                .version(1)
                .status(BomStatus.DRAFT)
                .createdBy(currentUser)
                .components(new ArrayList<>())
                .operations(new ArrayList<>())
                .build();

        bom = bomRepository.save(bom);

        // Add components
        if (request.getComponents() != null) {
            for (BomComponentRequest compReq : request.getComponents()) {
                addComponent(bom, compReq);
            }
        }

        // Add operations
        if (request.getOperations() != null) {
            int sequence = 1;
            for (BomOperationRequest opReq : request.getOperations()) {
                addOperation(bom, opReq, sequence++);
            }
        }

        // Add attachments
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            addAttachmentsToBom(bom, request.getAttachmentIds());
        }

        // Audit log
        auditService.logAction(AuditService.BOM_CREATED,
                "BOM: " + bom.getReference() + " for Product: " + product.getName(),
                null, "Created with version 1");

        log.info("BOM created: {} for product: {} by user: {}",
                bom.getReference(), product.getName(), currentUser.getLoginId());

        return mapToResponse(bom);
    }

    /**
     * Activate a BOM (Admin only) - moves from DRAFT to ACTIVE
     */
    @Transactional
    public BomResponse activateBom(UUID bomId) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can activate BOMs");
        }

        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found: " + bomId));

        if (bom.getStatus() != BomStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT BOMs can be activated");
        }

        // Check if product is active
        if (bom.getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Cannot activate BOM for non-active product");
        }

        // Archive any existing active BOM for this product
        bomRepository.findActiveByProductId(bom.getProduct().getId())
                .ifPresent(activeBom -> {
                    activeBom.setStatus(BomStatus.ARCHIVED);
                    bomRepository.save(activeBom);
                });

        bom.setStatus(BomStatus.ACTIVE);
        bom = bomRepository.save(bom);

        auditService.logAction(AuditService.BOM_ACTIVATED, "BOM: " + bom.getReference(),
                "DRAFT", "ACTIVE");

        log.info("BOM activated: {}", bom.getReference());

        return mapToResponse(bom);
    }

    /**
     * Update a BOM
     */
    @Transactional
    public BomResponse updateBom(UUID bomId, CreateBomRequest request) {
        if (!securityUtils.canCreateOrModify()) {
            throw new UnauthorizedException("Only Engineering users or Admins can update BOMs");
        }

        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found: " + bomId));

        if (bom.getStatus() != BomStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT BOMs can be updated directly. For active BOMs, please use an ECO.");
        }

        if (!bom.getReference().equals(request.getReference()) && 
            bomRepository.existsByReference(request.getReference())) {
            throw new DuplicateResourceException("A BOM with reference '" + request.getReference() + "' already exists");
        }

        bom.setReference(request.getReference());
        if (request.getQuantity() != null) {
            bom.setQuantity(request.getQuantity());
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
        bom.setProduct(product);
        bom = bomRepository.save(bom);

        // Recreate components and operations
        componentRepository.deleteAll(componentRepository.findByBomId(bomId));
        operationRepository.deleteAll(operationRepository.findByBomIdOrderBySequenceAsc(bomId));

        if (request.getComponents() != null) {
            for (BomComponentRequest compReq : request.getComponents()) {
                addComponent(bom, compReq);
            }
        }
        if (request.getOperations() != null) {
            int sequence = 1;
            for (BomOperationRequest opReq : request.getOperations()) {
                addOperation(bom, opReq, sequence++);
            }
        }

        return mapToResponse(bom);
    }

    /**
     * Delete a BOM
     */
    @Transactional
    public void deleteBom(UUID bomId) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can delete BOMs");
        }

        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found: " + bomId));

        if (bom.getStatus() == BomStatus.ACTIVE) {
            throw new BadRequestException("Active BOMs cannot be deleted.");
        }

        componentRepository.deleteAll(componentRepository.findByBomId(bomId));
        operationRepository.deleteAll(operationRepository.findByBomIdOrderBySequenceAsc(bomId));
        bomRepository.delete(bom);
    }

    /**
     * Get active BOMs (visible to all roles)
     */
    @Transactional(readOnly = true)
    public BomListResponse getActiveBoms(Pageable pageable) {
        Page<Bom> boms = bomRepository.findByStatus(BomStatus.ACTIVE, pageable);
        return buildListResponse(boms);
    }

    /**
     * Get all BOMs (visible to Engineering, Approver, Admin - not Operations)
     */
    @Transactional(readOnly = true)
    public BomListResponse getAllBoms(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();

        // Operations users can only see ACTIVE BOMs
        if (currentUser.getRole() == Role.OPERATIONS_USER) {
            return getActiveBoms(pageable);
        }

        Page<Bom> boms = bomRepository.findByStatusNot(BomStatus.ARCHIVED, pageable);
        return buildListResponse(boms);
    }

    /**
     * Get BOM by ID with components and operations
     */
    @Transactional(readOnly = true)
    public BomResponse getBomById(UUID bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found: " + bomId));

        User currentUser = securityUtils.getCurrentUser();

        // Operations users can only see ACTIVE or ARCHIVED BOMs
        if (currentUser.getRole() == Role.OPERATIONS_USER && bom.getStatus() == BomStatus.DRAFT) {
            throw new UnauthorizedException("You don't have permission to view draft BOMs");
        }

        return mapToResponse(bom);
    }

    /**
     * Get BOMs for a product
     */
    @Transactional(readOnly = true)
    public BomListResponse getBomsByProduct(UUID productId, Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Bom> boms;

        // Operations users can only see ACTIVE BOMs
        if (currentUser.getRole() == Role.OPERATIONS_USER) {
            boms = bomRepository.searchBoms(null, BomStatus.ACTIVE, productId, pageable);
        } else {
            boms = bomRepository.findByProductId(productId, pageable);
        }

        return buildListResponse(boms);
    }

    /**
     * Search BOMs with filters
     */
    @Transactional(readOnly = true)
    public BomListResponse searchBoms(BomSearchRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Operations users can only search ACTIVE BOMs
        BomStatus status = request.getStatus();
        if (currentUser.getRole() == Role.OPERATIONS_USER && status == null) {
            status = BomStatus.ACTIVE;
        }

        Page<Bom> boms = bomRepository.searchBoms(request.getReference(), status, request.getProductId(), pageable);
        return buildListResponse(boms);
    }

    /**
     * Internal method: Create new version of BOM (called by ECO apply)
     */
    @Transactional
    public Bom createNewVersion(Bom originalBom, User actor) {
        // Archive the original
        originalBom.setStatus(BomStatus.ARCHIVED);
        bomRepository.save(originalBom);

        // Create new version
        Bom newVersion = Bom.builder()
                .product(originalBom.getProduct())
                .reference(originalBom.getReference())
                .quantity(originalBom.getQuantity())
                .version(originalBom.getVersion() + 1)
                .status(BomStatus.ACTIVE)
                .createdBy(actor)
                .components(new ArrayList<>())
                .operations(new ArrayList<>())
                .build();

        newVersion = bomRepository.save(newVersion);

        // Copy components to new version
        List<BomComponent> originalComponents = componentRepository.findByBomId(originalBom.getId());
        for (BomComponent comp : originalComponents) {
            BomComponent newComp = BomComponent.builder()
                    .bom(newVersion)
                    .componentProduct(comp.getComponentProduct())
                    .quantity(comp.getQuantity())
                    .unit(comp.getUnit())
                    .build();
            componentRepository.save(newComp);
        }

        // Copy attachments to new version
        List<BomAttachment> originalAttachments = attachmentRepository.findByBomId(originalBom.getId());
        for (BomAttachment att : originalAttachments) {
            BomAttachment newAtt = BomAttachment.builder()
                    .bom(newVersion)
                    .fileUrl(att.getFileUrl())
                    .fileName(att.getFileName())
                    .fileType(att.getFileType())
                    .build();
            attachmentRepository.save(newAtt);
        }

        // Copy operations to new version
        List<BomOperation> originalOperations = operationRepository.findByBomIdOrderBySequenceAsc(originalBom.getId());
        for (BomOperation op : originalOperations) {
            BomOperation newOp = BomOperation.builder()
                    .bom(newVersion)
                    .name(op.getName())
                    .workCenter(op.getWorkCenter())
                    .expectedDurationMinutes(op.getExpectedDurationMinutes())
                    .sequence(op.getSequence())
                    .build();
            operationRepository.save(newOp);
        }

        auditService.logAction(AuditService.BOM_ARCHIVED, "BOM: " + originalBom.getReference(),
                "Version " + originalBom.getVersion(), "ARCHIVED");

        log.info("New BOM version created: {} v{}", newVersion.getReference(), newVersion.getVersion());

        return newVersion;
    }

    private void addAttachmentsToBom(Bom bom, List<UUID> fileIds) {
        for (UUID fileId : fileIds) {
            FileMetadata file = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

            BomAttachment attachment = BomAttachment.builder()
                    .bom(bom)
                    .fileUrl(fileStorageService.getFileUrl(file.getStoragePath()))
                    .fileName(file.getOriginalFileName())
                    .fileType(file.getFileType())
                    .build();

            attachmentRepository.save(attachment);
        }
    }

    private void addComponent(Bom bom, BomComponentRequest request) {
        Product componentProduct = productRepository.findById(request.getComponentProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Component product not found: " + request.getComponentProductId()));

        BomComponent component = BomComponent.builder()
                .bom(bom)
                .componentProduct(componentProduct)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .build();

        componentRepository.save(component);
    }

    private void addOperation(Bom bom, BomOperationRequest request, int defaultSequence) {
        BomOperation operation = BomOperation.builder()
                .bom(bom)
                .name(request.getName())
                .workCenter(request.getWorkCenter())
                .expectedDurationMinutes(request.getExpectedDurationMinutes())
                .sequence(request.getSequence() != null ? request.getSequence() : defaultSequence)
                .build();

        operationRepository.save(operation);
    }

    private BomListResponse buildListResponse(Page<Bom> boms) {
        List<BomResponse> bomResponses = boms.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return BomListResponse.builder()
                .boms(bomResponses)
                .totalElements(boms.getTotalElements())
                .totalPages(boms.getTotalPages())
                .currentPage(boms.getNumber())
                .pageSize(boms.getSize())
                .hasNext(boms.hasNext())
                .hasPrevious(boms.hasPrevious())
                .build();
    }

    private BomResponse mapToResponse(Bom bom) {
        List<BomComponent> components = componentRepository.findByBomId(bom.getId());
        List<BomOperation> operations = operationRepository.findByBomIdOrderBySequenceAsc(bom.getId());

        List<BomComponentResponse> componentResponses = components.stream()
                .map(c -> BomComponentResponse.builder()
                        .id(c.getId())
                        .componentProductId(c.getComponentProduct().getId())
                        .componentProductName(c.getComponentProduct().getName())
                        .quantity(c.getQuantity())
                        .unit(c.getUnit())
                        .build())
                .toList();

        List<BomOperationResponse> operationResponses = operations.stream()
                .map(o -> BomOperationResponse.builder()
                        .id(o.getId())
                        .name(o.getName())
                        .workCenter(o.getWorkCenter())
                        .expectedDurationMinutes(o.getExpectedDurationMinutes())
                        .build())
                .toList();

        List<BomAttachment> attachments = attachmentRepository.findByBomId(bom.getId());
        List<FileResponse> attachmentResponses = attachments.stream()
                .map(att -> FileResponse.builder()
                        .id(att.getId())
                        .fileName(att.getFileName())
                        .originalFileName(att.getFileName())
                        .fileType(att.getFileType())
                        .fileUrl(att.getFileUrl())
                        .build())
                .toList();

        return BomResponse.builder()
                .id(bom.getId())
                .productId(bom.getProduct() != null ? bom.getProduct().getId() : null)
                .productName(bom.getProduct() != null ? bom.getProduct().getName() : null)
                .reference(bom.getReference())
                .version(bom.getVersion())
                .status(bom.getStatus())
                .components(componentResponses)
                .operations(operationResponses)
                .attachments(attachmentResponses)
                .createdById(bom.getCreatedBy() != null ? bom.getCreatedBy().getId() : null)
                .createdByName(bom.getCreatedBy() != null ? bom.getCreatedBy().getFirstName() + " " + bom.getCreatedBy().getLastName() : null)
                .createdAt(bom.getCreatedAt())
                .updatedAt(bom.getUpdatedAt())
                .build();
    }
}
