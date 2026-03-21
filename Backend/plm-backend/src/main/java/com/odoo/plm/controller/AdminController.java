package com.odoo.plm.controller;

import com.odoo.plm.dto.request.admin.*;
import com.odoo.plm.dto.response.admin.*;
import com.odoo.plm.dto.response.eco.EcoStageResponse;
import com.odoo.plm.service.EcoApprovalRuleService;
import com.odoo.plm.service.EcoStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EcoStageService stageService;
    private final EcoApprovalRuleService approvalRuleService;

    // ==================== ECO Stages ====================

    @PostMapping("/stages")
    public ResponseEntity<EcoStageResponse> createStage(@Valid @RequestBody CreateEcoStageRequest request) {
        EcoStageResponse response = stageService.createStage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/stages/{id}")
    public ResponseEntity<EcoStageResponse> updateStage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEcoStageRequest request) {
        EcoStageResponse response = stageService.updateStage(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable UUID id) {
        stageService.deleteStage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stages")
    public ResponseEntity<List<EcoStageResponse>> getAllStages() {
        List<EcoStageResponse> response = stageService.getAllStages();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stages/reorder")
    public ResponseEntity<List<EcoStageResponse>> reorderStages(@Valid @RequestBody ReorderStagesRequest request) {
        List<EcoStageResponse> response = stageService.reorderStages(request);
        return ResponseEntity.ok(response);
    }

    // ==================== Approval Rules ====================

    @PostMapping("/approval-rules")
    public ResponseEntity<ApprovalRuleResponse> createApprovalRule(@Valid @RequestBody CreateApprovalRuleRequest request) {
        ApprovalRuleResponse response = approvalRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/approval-rules/{id}")
    public ResponseEntity<Void> deleteApprovalRule(@PathVariable UUID id) {
        approvalRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/approval-rules")
    public ResponseEntity<List<ApprovalRuleResponse>> getAllApprovalRules() {
        List<ApprovalRuleResponse> response = approvalRuleService.getAllRules();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approval-rules/stage/{stageId}")
    public ResponseEntity<List<ApprovalRuleResponse>> getApprovalRulesForStage(@PathVariable UUID stageId) {
        List<ApprovalRuleResponse> response = approvalRuleService.getRulesForStage(stageId);
        return ResponseEntity.ok(response);
    }
}
