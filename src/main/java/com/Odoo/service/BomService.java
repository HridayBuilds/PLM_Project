package com.Odoo.service;

import com.Odoo.dto.*;
import com.Odoo.model.*;
import com.Odoo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BomService {

    private final BomRepository bomRepository;
    private final BomLineRepository bomLineRepository;
    private final ProductRepository productRepository;

    public Page<BomDTO> getAllBoms(Long productId, Pageable pageable) {
        Page<Bom> boms;
        if (productId != null) {
            boms = bomRepository.findByProductIdAndActiveTrue(productId, pageable);
        } else {
            boms = bomRepository.findByActiveTrue(pageable);
        }
        return boms.map(this::toDTO);
    }

    public BomDTO getBomById(Long id) {
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM not found: " + id));
        return toDTO(bom);
    }

    public BomDTO createBom(BomDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));

        Bom bom = new Bom();
        bom.setProduct(product);
        bom.setReference(dto.getReference());
        bom.setQuantity(dto.getQuantity());
        bom.setActive(true);
        bom.setVersion(1);
        bom.setCreatedAt(LocalDateTime.now());
        bom = bomRepository.save(bom);

        // Create BOM lines
        if (dto.getLines() != null) {
            for (BomLineDTO lineDTO : dto.getLines()) {
                createBomLine(bom, lineDTO);
            }
        }

        log.info("Created BOM: {} for product: {}", bom.getReference(), product.getName());
        return toDTO(bomRepository.findById(bom.getId()).orElse(bom));
    }

    public BomDTO updateBom(Long id, BomDTO dto) {
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM not found: " + id));

        bom.setReference(dto.getReference());
        bom.setQuantity(dto.getQuantity());
        bom.setUpdatedAt(LocalDateTime.now());

        // Update lines
        if (dto.getLines() != null) {
            // Remove existing lines
            bomLineRepository.deleteByBomId(id);

            // Create new lines
            for (BomLineDTO lineDTO : dto.getLines()) {
                createBomLine(bom, lineDTO);
            }
        }

        bom = bomRepository.save(bom);
        log.info("Updated BOM: {}", id);
        return toDTO(bomRepository.findById(bom.getId()).orElse(bom));
    }

    public void deleteBom(Long id) {
        Bom bom = bomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BOM not found: " + id));
        bom.setActive(false);
        bomRepository.save(bom);
        log.info("Deactivated BOM: {}", id);
    }

    public BomLineDTO addBomLine(Long bomId, BomLineDTO dto) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new RuntimeException("BOM not found: " + bomId));

        BomLine line = createBomLine(bom, dto);
        log.info("Added line to BOM {}: component {}", bomId, dto.getComponentId());
        return toLineDTO(line);
    }

    public BomLineDTO updateBomLine(Long lineId, BomLineDTO dto) {
        BomLine line = bomLineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("BOM line not found: " + lineId));

        if (dto.getComponentId() != null) {
            Product component = productRepository.findById(dto.getComponentId())
                    .orElseThrow(() -> new RuntimeException("Component not found: " + dto.getComponentId()));
            line.setComponent(component);
        }

        line.setQuantity(dto.getQuantity());
        line.setSequence(dto.getSequence());
        line = bomLineRepository.save(line);

        log.info("Updated BOM line: {}", lineId);
        return toLineDTO(line);
    }

    public void deleteBomLine(Long lineId) {
        bomLineRepository.deleteById(lineId);
        log.info("Deleted BOM line: {}", lineId);
    }

    public BigDecimal calculateTotalCost(Long bomId) {
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new RuntimeException("BOM not found: " + bomId));

        BigDecimal totalCost = BigDecimal.ZERO;
        List<BomLine> lines = bomLineRepository.findByBomIdOrderBySequence(bomId);

        for (BomLine line : lines) {
            if (line.getComponent().getCost() != null) {
                BigDecimal lineCost = line.getComponent().getCost()
                        .multiply(line.getQuantity());
                totalCost = totalCost.add(lineCost);
            }
        }

        return totalCost.divide(bom.getQuantity(), 2, BigDecimal.ROUND_HALF_UP);
    }

    public List<BomComparisonDTO> compareBomVersions(Long bomId1, Long bomId2) {
        List<BomLine> lines1 = bomLineRepository.findByBomIdOrderBySequence(bomId1);
        List<BomLine> lines2 = bomLineRepository.findByBomIdOrderBySequence(bomId2);

        List<BomComparisonDTO> comparison = new ArrayList<>();

        // Compare components
        for (BomLine line1 : lines1) {
            BomComparisonDTO dto = new BomComparisonDTO();
            dto.setComponentId(line1.getComponent().getId());
            dto.setComponentName(line1.getComponent().getName());
            dto.setOldQuantity(line1.getQuantity());

            // Find matching line in second BOM
            BomLine matchingLine = lines2.stream()
                    .filter(l -> l.getComponent().getId().equals(line1.getComponent().getId()))
                    .findFirst()
                    .orElse(null);

            if (matchingLine != null) {
                dto.setNewQuantity(matchingLine.getQuantity());
                dto.setChangeType("MODIFIED");
            } else {
                dto.setNewQuantity(BigDecimal.ZERO);
                dto.setChangeType("REMOVED");
            }

            comparison.add(dto);
        }

        // Find added components
        for (BomLine line2 : lines2) {
            boolean existsInFirst = lines1.stream()
                    .anyMatch(l -> l.getComponent().getId().equals(line2.getComponent().getId()));

            if (!existsInFirst) {
                BomComparisonDTO dto = new BomComparisonDTO();
                dto.setComponentId(line2.getComponent().getId());
                dto.setComponentName(line2.getComponent().getName());
                dto.setOldQuantity(BigDecimal.ZERO);
                dto.setNewQuantity(line2.getQuantity());
                dto.setChangeType("ADDED");
                comparison.add(dto);
            }
        }

        return comparison;
    }

    private BomLine createBomLine(Bom bom, BomLineDTO dto) {
        Product component = productRepository.findById(dto.getComponentId())
                .orElseThrow(() -> new RuntimeException("Component not found: " + dto.getComponentId()));

        BomLine line = new BomLine();
        line.setBom(bom);
        line.setComponent(component);
        line.setQuantity(dto.getQuantity());
        line.setSequence(dto.getSequence() != null ? dto.getSequence() : 0);

        return bomLineRepository.save(line);
    }

    private BomDTO toDTO(Bom bom) {
        BomDTO dto = new BomDTO();
        dto.setId(bom.getId());
        dto.setProductId(bom.getProduct().getId());
        dto.setProductName(bom.getProduct().getName());
        dto.setReference(bom.getReference());
        dto.setQuantity(bom.getQuantity());
        dto.setVersion(bom.getVersion());
        dto.setActive(bom.getActive());

        // Get lines
        List<BomLine> lines = bomLineRepository.findByBomIdOrderBySequence(bom.getId());
        dto.setLines(lines.stream().map(this::toLineDTO).collect(Collectors.toList()));

        // Calculate total cost
        dto.setTotalCost(calculateTotalCost(bom.getId()));

        return dto;
    }

    private BomLineDTO toLineDTO(BomLine line) {
        BomLineDTO dto = new BomLineDTO();
        dto.setId(line.getId());
        dto.setBomId(line.getBom().getId());
        dto.setComponentId(line.getComponent().getId());
        dto.setComponentName(line.getComponent().getName());
        dto.setComponentReference(line.getComponent().getInternalReference());
        dto.setQuantity(line.getQuantity());
        dto.setSequence(line.getSequence());
        dto.setUnitCost(line.getComponent().getCost());

        if (line.getComponent().getCost() != null) {
            dto.setTotalCost(line.getComponent().getCost().multiply(line.getQuantity()));
        }

        return dto;
    }
}
