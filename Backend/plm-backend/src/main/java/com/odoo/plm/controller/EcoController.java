package com.odoo.plm.controller;

import com.odoo.plm.dto.request.eco.*;
import com.odoo.plm.dto.response.eco.*;
import com.odoo.plm.service.EcoService;
import com.odoo.plm.service.EcoStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ecos")
@RequiredArgsConstructor
public class EcoController {

    private final EcoService ecoService;
    private final EcoStageService ecoStageService;

    @GetMapping
    public ResponseEntity<EcoListResponse> getAllEcos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        EcoListResponse response = ecoService.getAllEcos(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> createEco(@Valid @RequestBody CreateEcoRequest request) {
        EcoResponse response = ecoService.createEco(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> updateEco(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEcoRequest request) {
        EcoResponse response = ecoService.updateEco(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<Void> deleteEco(@PathVariable UUID id) {
        ecoService.deleteEco(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/bom-changes")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> addBomChange(
            @PathVariable UUID id,
            @Valid @RequestBody EcoBomChangeRequest request) {
        EcoResponse response = ecoService.addBomChange(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/product-changes")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> addProductChange(
            @PathVariable UUID id,
            @Valid @RequestBody EcoProductChangeRequest request) {
        EcoResponse response = ecoService.addProductChange(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ecoId}/changes/{changeId}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> removeChange(
            @PathVariable UUID ecoId,
            @PathVariable UUID changeId) {
        EcoResponse response = ecoService.removeChange(ecoId, changeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/bom-operation-changes")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> addBomOperationChange(
            @PathVariable UUID id,
            @Valid @RequestBody EcoBomOperationChangeRequest request) {
        EcoResponse response = ecoService.addBomOperationChange(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ecoId}/bom-operation-changes/{changeId}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> removeBomOperationChange(
            @PathVariable UUID ecoId,
            @PathVariable UUID changeId) {
        EcoResponse response = ecoService.removeBomOperationChange(ecoId, changeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/attachments", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> addAttachment(
            @PathVariable UUID id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        EcoResponse response = ecoService.addAttachment(id, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ecoId}/attachments/{attachmentId}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> removeAttachment(
            @PathVariable UUID ecoId,
            @PathVariable UUID attachmentId) {
        EcoResponse response = ecoService.removeAttachment(ecoId, attachmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<EcoResponse> submitForApproval(@PathVariable UUID id) {
        EcoResponse response = ecoService.submitForApproval(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<EcoResponse> approveEco(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveEcoRequest request) {
        EcoResponse response = ecoService.approveEco(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EcoResponse> getEcoById(@PathVariable UUID id) {
        EcoResponse response = ecoService.getEcoById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<EcoListResponse> getMyEcos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        EcoListResponse response = ecoService.getMyEcos(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<EcoListResponse> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        EcoListResponse response = ecoService.getPendingApprovals(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<EcoListResponse> searchEcos(@Valid @RequestBody EcoSearchRequest request) {
        EcoListResponse response = ecoService.searchEcos(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/comparison")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'ADMIN')")
    public ResponseEntity<EcoComparisonResponse> getComparison(@PathVariable UUID id) {
        EcoComparisonResponse response = ecoService.getComparison(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all stages (for all authenticated users).
     * This endpoint is different from /api/admin/stages which is admin-only.
     */
    @GetMapping("/stages")
    public ResponseEntity<List<EcoStageResponse>> getAllStages(
            @RequestParam(required = false) UUID productId) {
        List<EcoStageResponse> response;
        if (productId != null) {
            response = ecoStageService.getStagesByProduct(productId);
        } else {
            response = ecoStageService.getAllStages();
        }
        return ResponseEntity.ok(response);
    }
}
