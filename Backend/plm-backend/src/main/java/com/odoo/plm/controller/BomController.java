package com.odoo.plm.controller;

import com.odoo.plm.dto.request.bom.BomSearchRequest;
import com.odoo.plm.dto.request.bom.CreateBomRequest;
import com.odoo.plm.dto.response.bom.BomListResponse;
import com.odoo.plm.dto.response.bom.BomResponse;
import com.odoo.plm.service.BomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'ADMIN')")
    public ResponseEntity<BomResponse> createBom(@Valid @RequestBody CreateBomRequest request) {
        BomResponse response = bomService.createBom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BomResponse> activateBom(@PathVariable UUID id) {
        BomResponse response = bomService.activateBom(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BomResponse> getBomById(@PathVariable UUID id) {
        BomResponse response = bomService.getBomById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BomListResponse> getAllBoms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        BomListResponse response = bomService.getAllBoms(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<BomListResponse> getActiveBoms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reference") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        BomListResponse response = bomService.getActiveBoms(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<BomListResponse> getBomsByProduct(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        BomListResponse response = bomService.getBomsByProduct(productId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<BomListResponse> searchBoms(@Valid @RequestBody BomSearchRequest request) {
        BomListResponse response = bomService.searchBoms(request);
        return ResponseEntity.ok(response);
    }
}
