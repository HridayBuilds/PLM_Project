package com.odoo.plm.controller;

import com.odoo.plm.dto.response.report.AuditLogResponse;
import com.odoo.plm.service.AuditService;
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

    @GetMapping("/audit-logs/eco/{ecoId}")
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('ENGINEERING_USER', 'APPROVER', 'ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getRecentActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> response = auditService.getRecentActivity(pageable);
        return ResponseEntity.ok(response);
    }
}
