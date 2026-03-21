package com.odoo.plm.dto.response.eco;

import com.odoo.plm.enums.ApprovalDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoApprovalResponse {

    private UUID id;
    private UUID approverId;
    private String approverName;
    private UUID stageId;
    private String stageName;
    private ApprovalDecision decision;
    private String comments;
    private LocalDateTime approvedAt;
}
