package com.odoo.plm.controller;

import com.odoo.plm.dto.response.report.AuditLogResponse;
import com.odoo.plm.dto.response.report.EcoReportResponse;
import com.odoo.plm.dto.response.report.ProductVersionHistoryResponse;
import com.odoo.plm.dto.response.report.BomChangeHistoryResponse;
import com.odoo.plm.dto.response.report.ArchivedProductResponse;
import com.odoo.plm.dto.response.report.ProductBomMatrixResponse;
import com.odoo.plm.service.AuditService;
import com.odoo.plm.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AuditService auditService;
    private final ReportService reportService;

    // ============ ECO Report ============
    @GetMapping("/eco")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<List<EcoReportResponse>> getEcoReport(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        List<EcoReportResponse> response = reportService.getEcoReport(type, stage, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ============ Product Version History ============
    @GetMapping("/products/{productId}/versions")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<List<ProductVersionHistoryResponse>> getProductVersionHistory(
            @PathVariable UUID productId) {
        List<ProductVersionHistoryResponse> response = reportService.getProductVersionHistory(productId);
        return ResponseEntity.ok(response);
    }

    // ============ BOM Change History ============
    @GetMapping("/products/{productId}/bom-changes")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<List<BomChangeHistoryResponse>> getBomChangeHistory(
            @PathVariable UUID productId) {
        List<BomChangeHistoryResponse> response = reportService.getBomChangeHistory(productId);
        return ResponseEntity.ok(response);
    }

    // ============ Archived Products ============
    @GetMapping("/products/archived")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<List<ArchivedProductResponse>> getArchivedProducts() {
        List<ArchivedProductResponse> response = reportService.getArchivedProducts();
        return ResponseEntity.ok(response);
    }

    // ============ Product-BOM Matrix ============
    @GetMapping("/product-bom-matrix")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<List<ProductBomMatrixResponse>> getProductBomMatrix() {
        List<ProductBomMatrixResponse> response = reportService.getProductBomMatrix();
        return ResponseEntity.ok(response);
    }

    // ============ Audit Logs (Existing) ============
    @GetMapping("/audit-logs/eco/{ecoId}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsForEco(@PathVariable UUID ecoId) {
        List<AuditLogResponse> response = auditService.getLogsForEco(ecoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-logs/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLogResponse> response = auditService.getLogsByActor(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> searchAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLogResponse> response = auditService.searchLogs(action, actorId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-logs/recent")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'OPERATIONS_USER', 'ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getRecentActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> response = auditService.getRecentActivity(pageable);
        return ResponseEntity.ok(response);
    }
}
