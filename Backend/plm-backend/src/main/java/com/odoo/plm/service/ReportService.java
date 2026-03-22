package com.odoo.plm.service;

import com.odoo.plm.dto.response.report.*;
import com.odoo.plm.entity.*;
import com.odoo.plm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final EcoRepository ecoRepository;
    private final ProductRepository productRepository;
    private final BomRepository bomRepository;

    public List<EcoReportResponse> getEcoReport(String type, String stage, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching ECO Report");
        return ecoRepository.findAll().stream().map(eco -> EcoReportResponse.builder()
                .ecoId(eco.getId())
                .title(eco.getTitle())
                .description(eco.getDescription())
                .ecoType(eco.getEcoType())
                .status(eco.getStatus())
                .versionUpdate(eco.getVersionUpdate())
                .effectiveDate(eco.getEffectiveDate())
                .stageName(eco.getCurrentStage() != null ? eco.getCurrentStage().getName() : null)
                .productName(eco.getProduct() != null ? eco.getProduct().getName() : null)
                .createdByName(eco.getCreatedBy() != null ? eco.getCreatedBy().getFirstName() + " " + eco.getCreatedBy().getLastName() : null)
                .createdAt(eco.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    public List<ProductVersionHistoryResponse> getProductVersionHistory(UUID productId) {
        log.info("Fetching Product Version History for {}", productId);
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return Collections.emptyList();
        
        List<Product> allVersions = productRepository.findAllVersionsByName(product.getName());
        
        List<ProductVersionHistoryResponse.VersionDetail> details = allVersions.stream().map(p -> 
            ProductVersionHistoryResponse.VersionDetail.builder()
                .productId(p.getId())
                .version(p.getVersion())
                .status(p.getStatus())
                .salePrice(p.getSalePrice())
                .costPrice(p.getCostPrice())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .build()
        ).collect(Collectors.toList());

        return List.of(ProductVersionHistoryResponse.builder()
                .productName(product.getName())
                .versions(details)
                .build());
    }

    public List<BomChangeHistoryResponse> getBomChangeHistory(UUID productId) {
        log.info("Fetching BOM Change History for {}", productId);
        return Collections.emptyList(); 
    }

    public List<ArchivedProductResponse> getArchivedProducts() {
        log.info("Fetching Archived Products");
        return productRepository.findByStatus(com.odoo.plm.enums.ProductStatus.ARCHIVED).stream()
            .map(p -> ArchivedProductResponse.builder()
                .productId(p.getId())
                .name(p.getName())
                .version(p.getVersion())
                .build())
            .collect(Collectors.toList());
    }

    public List<ProductBomMatrixResponse> getProductBomMatrix() {
        log.info("Fetching Product BOM Matrix");
        return productRepository.findAll().stream()
            .map(p -> ProductBomMatrixResponse.builder()
                .productId(p.getId())
                .productName(p.getName())
                .activeVersion(p.getVersion())
                .build())
            .collect(Collectors.toList());
    }
}
