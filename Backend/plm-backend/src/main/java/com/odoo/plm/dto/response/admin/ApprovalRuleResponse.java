package com.odoo.plm.dto.response.admin;

import com.odoo.plm.enums.ApprovalCategory;
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
public class ApprovalRuleResponse {

    private UUID id;
    private UUID stageId;
    private String stageName;
    private UUID approverUserId;
    private String approverName;
    private String approverEmail;
    private ApprovalCategory category;
    private LocalDateTime createdAt;
}
