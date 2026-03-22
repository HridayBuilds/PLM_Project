package com.ecotracker.service;

import com.ecotracker.dto.*;
import com.ecotracker.model.*;
import com.ecotracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BomChangeService {

    private final EcoBomChangeRepository bomChangeRepository;
    private final EcoRepository ecoRepository;
    private final BomRepository bomRepository;
    private final BomLineRepository bomLineRepository;
    private final ProductRepository productRepository;

    /**
     * Add a BOM change to an ECO
     */
    public BomChangeResponseDTO addBomChange(Long ecoId, BomChangeCreateDTO dto) {
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new RuntimeException("ECO not found"));

        if (eco.getState() == Eco.EcoState.DONE) {
            throw new RuntimeException("Cannot add changes to a completed ECO");
        }

        Bom bom = bomRepository.findById(dto.getBomId())
                .orElseThrow(() -> new RuntimeException("BOM not found"));

        EcoBomChange change = new EcoBomChange();
        change.setEco(eco);
        change.setBom(bom);
        change.setChangeType(dto.getChangeType());
        change.setDescription(dto.getDescription());

        // Set BOM line for update/remove/replace operations
        if (dto.getBomLineId() != null) {
            BomLine bomLine = bomLineRepository.findById(dto.getBomLineId())
                    .orElseThrow(() -> new RuntimeException("BOM Line not found"));
            change.setBomLine(bomLine);
            change.setOldProduct(bomLine.getProduct());
            change.setOldQuantity(bomLine.getQuantity());
            change.setOldOperation(bomLine.getOperation());
        }

        // Set new values
        if (dto.getNewProductId() != null) {
            Product newProduct = productRepository.findById(dto.getNewProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            change.setNewProduct(newProduct);
        }

        change.setNewQuantity(dto.getNewQuantity());
        change.setNewOperation(dto.getNewOperation());

        change = bomChangeRepository.save(change);
        log.info("Added BOM change {} to ECO {}", change.getId(), eco.getReference());

        return mapToResponseDTO(change);
    }

    /**
     * Update a BOM change
     */
    public BomChangeResponseDTO updateBomChange(Long changeId, BomChangeUpdateDTO dto) {
        EcoBomChange change = bomChangeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("BOM Change not found"));

        if (change.getEco().getState() == Eco.EcoState.DONE) {
            throw new RuntimeException("Cannot update changes in a completed ECO");
        }

        if (dto.getNewProductId() != null) {
            Product newProduct = productRepository.findById(dto.getNewProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            change.setNewProduct(newProduct);
        }

        if (dto.getNewQuantity() != null) {
            change.setNewQuantity(dto.getNewQuantity());
        }

        if (dto.getNewOperation() != null) {
            change.setNewOperation(dto.getNewOperation());
        }

        if (dto.getDescription() != null) {
            change.setDescription(dto.getDescription());
        }

        change = bomChangeRepository.save(change);
        return mapToResponseDTO(change);
    }

    /**
     * Remove a BOM change
     */
    public void removeBomChange(Long changeId) {
        EcoBomChange change = bomChangeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("BOM Change not found"));

        if (change.getEco().getState() == Eco.EcoState.DONE) {
            throw new RuntimeException("Cannot remove changes from a completed ECO");
        }

        bomChangeRepository.delete(change);
        log.info("Removed BOM change {} from ECO {}", changeId, change.getEco().getReference());
    }

    /**
     * Get BOM changes for an ECO
     */
    @Transactional(readOnly = true)
    public List<BomChangeResponseDTO> getBomChanges(Long ecoId) {
        List<EcoBomChange> changes = bomChangeRepository.findByEcoId(ecoId);
        return changes.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get BOM change details
     */
    @Transactional(readOnly = true)
    public BomChangeResponseDTO getBomChange(Long changeId) {
        EcoBomChange change = bomChangeRepository.findById(changeId)
                .orElseThrow(() -> new RuntimeException("BOM Change not found"));
        return mapToResponseDTO(change);
    }

    /**
     * Preview BOM after changes would be applied
     */
    @Transactional(readOnly = true)
    public BomPreviewDTO previewBomChanges(Long ecoId) {
        Eco eco = ecoRepository.findById(ecoId)
                .orElseThrow(() -> new RuntimeException("ECO not found"));

        List<EcoBomChange> changes = bomChangeRepository.findByEcoId(ecoId);

        BomPreviewDTO preview = new BomPreviewDTO();
        preview.setEcoId(ecoId);
        preview.setEcoReference(eco.getReference());

        // Group changes by BOM
        var changesByBom = changes.stream()
                .collect(Collectors.groupingBy(c -> c.getBom().getId()));

        List<BomPreviewDTO.BomPreviewItem> items = changesByBom.entrySet().stream()
                .map(entry -> {
                    Bom bom = bomRepository.findById(entry.getKey()).orElse(null);
                    if (bom == null) return null;

                    BomPreviewDTO.BomPreviewItem item = new BomPreviewDTO.BomPreviewItem();
                    item.setBomId(bom.getId());
                    item.setBomReference(bom.getReference());
                    item.setProductName(bom.getProduct().getName());
                    item.setCurrentVersion(bom.getVersion());
                    item.setNewVersion(bom.getVersion() + 1);

                    // Build current and preview lines
                    List<BomLine> currentLines = bomLineRepository.findByBomId(bom.getId());
                    item.setCurrentLines(currentLines.stream()
                            .map(this::mapBomLineToDTO)
                            .collect(Collectors.toList()));

                    // Apply changes to create preview
                    item.setPreviewLines(applyChangesPreview(currentLines, entry.getValue()));
                    item.setChanges(entry.getValue().stream()
                            .map(this::mapToResponseDTO)
                            .collect(Collectors.toList()));

                    return item;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        preview.setBomPreviews(items);
        return preview;
    }

    // ==================== Helper Methods ====================

    private List<BomLineDTO> applyChangesPreview(List<BomLine> currentLines, List<EcoBomChange> changes) {
        // Create a mutable copy of current state
        List<BomLineDTO> previewLines = currentLines.stream()
                .map(this::mapBomLineToDTO)
                .collect(Collectors.toList());

        for (EcoBomChange change : changes) {
            switch (change.getChangeType()) {
                case ADD_COMPONENT:
                    BomLineDTO newLine = new BomLineDTO();
                    newLine.setId(-1L); // Negative ID indicates new
                    if (change.getNewProduct() != null) {
                        newLine.setProductId(change.getNewProduct().getId());
                        newLine.setProductName(change.getNewProduct().getName());
                        newLine.setProductReference(change.getNewProduct().getReference());
                    }
                    newLine.setQuantity(change.getNewQuantity());
                    newLine.setOperation(change.getNewOperation());
                    newLine.setChangeType("ADD");
                    previewLines.add(newLine);
                    break;

                case REMOVE_COMPONENT:
                    if (change.getBomLine() != null) {
                        previewLines.removeIf(l -> l.getId().equals(change.getBomLine().getId()));
                    }
                    break;

                case UPDATE_COMPONENT:
                case REPLACE_COMPONENT:
                    if (change.getBomLine() != null) {
                        previewLines.stream()
                                .filter(l -> l.getId().equals(change.getBomLine().getId()))
                                .findFirst()
                                .ifPresent(l -> {
                                    if (change.getNewProduct() != null) {
                                        l.setProductId(change.getNewProduct().getId());
                                        l.setProductName(change.getNewProduct().getName());
                                        l.setProductReference(change.getNewProduct().getReference());
                                    }
                                    if (change.getNewQuantity() != null) {
                                        l.setQuantity(change.getNewQuantity());
                                    }
                                    if (change.getNewOperation() != null) {
                                        l.setOperation(change.getNewOperation());
                                    }
                                    l.setChangeType(change.getChangeType().name());
                                });
                    }
                    break;
            }
        }

        return previewLines;
    }

    private BomLineDTO mapBomLineToDTO(BomLine line) {
        BomLineDTO dto = new BomLineDTO();
        dto.setId(line.getId());
        dto.setProductId(line.getProduct().getId());
        dto.setProductName(line.getProduct().getName());
        dto.setProductReference(line.getProduct().getReference());
        dto.setQuantity(line.getQuantity());
        dto.setOperation(line.getOperation());
        return dto;
    }

    private BomChangeResponseDTO mapToResponseDTO(EcoBomChange change) {
        BomChangeResponseDTO dto = new BomChangeResponseDTO();
        dto.setId(change.getId());
        dto.setChangeType(change.getChangeType());
        dto.setDescription(change.getDescription());
        dto.setCreatedAt(change.getCreatedAt());

        // Map BOM
        if (change.getBom() != null) {
            dto.setBomId(change.getBom().getId());
            dto.setBomReference(change.getBom().getReference());
            if (change.getBom().getProduct() != null) {
                dto.setBomProductName(change.getBom().getProduct().getName());
            }
        }

        // Map BOM Line
        if (change.getBomLine() != null) {
            dto.setBomLineId(change.getBomLine().getId());
        }

        // Map old values
        if (change.getOldProduct() != null) {
            dto.setOldProductId(change.getOldProduct().getId());
            dto.setOldProductName(change.getOldProduct().getName());
            dto.setOldProductReference(change.getOldProduct().getReference());
        }
        dto.setOldQuantity(change.getOldQuantity());
        dto.setOldOperation(change.getOldOperation());

        // Map new values
        if (change.getNewProduct() != null) {
            dto.setNewProductId(change.getNewProduct().getId());
            dto.setNewProductName(change.getNewProduct().getName());
            dto.setNewProductReference(change.getNewProduct().getReference());
        }
        dto.setNewQuantity(change.getNewQuantity());
        dto.setNewOperation(change.getNewOperation());

        return dto;
    }
}
