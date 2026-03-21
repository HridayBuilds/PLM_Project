package com.odoo.plm.service;

import com.odoo.plm.dto.request.product.CreateProductRequest;
import com.odoo.plm.dto.request.product.ProductSearchRequest;
import com.odoo.plm.dto.response.FileResponse;
import com.odoo.plm.dto.response.product.ProductListResponse;
import com.odoo.plm.dto.response.product.ProductResponse;
import com.odoo.plm.entity.FileMetadata;
import com.odoo.plm.entity.Product;
import com.odoo.plm.entity.ProductAttachment;
import com.odoo.plm.entity.User;
import com.odoo.plm.enums.ProductStatus;
import com.odoo.plm.enums.Role;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.DuplicateResourceException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.FileMetadataRepository;
import com.odoo.plm.repository.ProductAttachmentRepository;
import com.odoo.plm.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductAttachmentRepository attachmentRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    /**
     * Create a new product (Initial creation by Engineering/Admin)
     * Product starts as DRAFT with version 1
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Only ENGINEERING_USER or ADMIN can create products
        if (!securityUtils.canCreateOrModify()) {
            throw new UnauthorizedException("Only Engineering users or Admins can create products");
        }

        // Check for duplicate active product name
        if (productRepository.existsByNameAndStatus(request.getName(), ProductStatus.ACTIVE)) {
            throw new DuplicateResourceException("An active product with name '" + request.getName() + "' already exists");
        }

        // Create product
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .version(1)
                .status(ProductStatus.DRAFT)
                .salePrice(request.getSalePrice())
                .costPrice(request.getCostPrice())
                .createdBy(currentUser)
                .attachments(new ArrayList<>())
                .build();

        product = productRepository.save(product);

        // Add attachments if provided
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            addAttachmentsToProduct(product, request.getAttachmentIds());
        }

        // Audit log
        auditService.logAction(AuditService.PRODUCT_CREATED, "Product: " + product.getName(),
                null, "Created with version 1");

        log.info("Product created: {} by user: {}", product.getName(), currentUser.getLoginId());

        return mapToResponse(product);
    }

    /**
     * Activate a product (Admin only) - moves from DRAFT to ACTIVE
     */
    @Transactional
    public ProductResponse activateProduct(UUID productId) {
        if (!securityUtils.isAdmin()) {
            throw new UnauthorizedException("Only Admins can activate products");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getStatus() != ProductStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT products can be activated");
        }

        // Check if there's already an active product with same name
        if (productRepository.existsByNameAndStatus(product.getName(), ProductStatus.ACTIVE)) {
            throw new BadRequestException("An active product with this name already exists");
        }

        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);

        auditService.logAction(AuditService.PRODUCT_ACTIVATED, "Product: " + product.getName(),
                "DRAFT", "ACTIVE");

        log.info("Product activated: {}", product.getName());

        return mapToResponse(product);
    }

    /**
     * Get active products (visible to all roles)
     */
    @Transactional(readOnly = true)
    public ProductListResponse getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return buildListResponse(products);
    }

    /**
     * Get all products (visible to Engineering, Approver, Admin - not Operations)
     */
    @Transactional(readOnly = true)
    public ProductListResponse getAllProducts(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();

        // Operations users can only see ACTIVE products
        if (currentUser.getRole() == Role.OPERATIONS_USER) {
            return getActiveProducts(pageable);
        }

        Page<Product> products = productRepository.findAll(pageable);
        return buildListResponse(products);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        User currentUser = securityUtils.getCurrentUser();

        // Operations users can only see ACTIVE or ARCHIVED products
        if (currentUser.getRole() == Role.OPERATIONS_USER && product.getStatus() == ProductStatus.DRAFT) {
            throw new UnauthorizedException("You don't have permission to view draft products");
        }

        return mapToResponse(product);
    }

    /**
     * Get version history for a product
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductVersionHistory(String productName) {
        List<Product> versions = productRepository.findAllVersionsByName(productName);
        if (versions.isEmpty()) {
            throw new ResourceNotFoundException("Product not found: " + productName);
        }
        return versions.stream().map(this::mapToResponse).toList();
    }

    /**
     * Search products with filters
     */
    @Transactional(readOnly = true)
    public ProductListResponse searchProducts(ProductSearchRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Sort sort = request.getSortDir().equalsIgnoreCase("asc")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Operations users can only search ACTIVE products
        ProductStatus status = request.getStatus();
        if (currentUser.getRole() == Role.OPERATIONS_USER && status == null) {
            status = ProductStatus.ACTIVE;
        }

        Page<Product> products = productRepository.searchProducts(request.getName(), status, pageable);
        return buildListResponse(products);
    }

    /**
     * Internal method: Create new version of product (called by ECO apply)
     */
    @Transactional
    public Product createNewVersion(Product originalProduct, User actor) {
        // Archive the original
        originalProduct.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(originalProduct);

        // Create new version
        Product newVersion = Product.builder()
                .name(originalProduct.getName())
                .description(originalProduct.getDescription())
                .version(originalProduct.getVersion() + 1)
                .status(ProductStatus.ACTIVE)
                .salePrice(originalProduct.getSalePrice())
                .costPrice(originalProduct.getCostPrice())
                .createdBy(actor)
                .attachments(new ArrayList<>())
                .build();

        newVersion = productRepository.save(newVersion);

        // Copy attachments to new version
        List<ProductAttachment> originalAttachments = attachmentRepository.findByProductId(originalProduct.getId());
        for (ProductAttachment att : originalAttachments) {
            ProductAttachment newAtt = ProductAttachment.builder()
                    .product(newVersion)
                    .fileUrl(att.getFileUrl())
                    .fileName(att.getFileName())
                    .fileType(att.getFileType())
                    .build();
            attachmentRepository.save(newAtt);
        }

        auditService.logAction(AuditService.PRODUCT_ARCHIVED, "Product: " + originalProduct.getName(),
                "Version " + originalProduct.getVersion(), "ARCHIVED");

        log.info("New product version created: {} v{}", newVersion.getName(), newVersion.getVersion());

        return newVersion;
    }

    private void addAttachmentsToProduct(Product product, List<UUID> fileIds) {
        for (UUID fileId : fileIds) {
            FileMetadata file = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

            ProductAttachment attachment = ProductAttachment.builder()
                    .product(product)
                    .fileUrl(fileStorageService.getFileUrl(file.getStoragePath()))
                    .fileName(file.getOriginalFileName())
                    .fileType(file.getFileType())
                    .build();

            attachmentRepository.save(attachment);
        }
    }

    private ProductListResponse buildListResponse(Page<Product> products) {
        List<ProductResponse> productResponses = products.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return ProductListResponse.builder()
                .products(productResponses)
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .currentPage(products.getNumber())
                .pageSize(products.getSize())
                .hasNext(products.hasNext())
                .hasPrevious(products.hasPrevious())
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        List<ProductAttachment> attachments = attachmentRepository.findByProductId(product.getId());

        List<FileResponse> attachmentResponses = attachments.stream()
                .map(att -> FileResponse.builder()
                        .id(att.getId())
                        .fileName(att.getFileName())
                        .originalFileName(att.getFileName())
                        .fileType(att.getFileType())
                        .fileUrl(att.getFileUrl())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .version(product.getVersion())
                .status(product.getStatus())
                .salePrice(product.getSalePrice())
                .costPrice(product.getCostPrice())
                .attachments(attachmentResponses)
                .createdById(product.getCreatedBy().getId())
                .createdByName(product.getCreatedBy().getFirstName() + " " + product.getCreatedBy().getLastName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
