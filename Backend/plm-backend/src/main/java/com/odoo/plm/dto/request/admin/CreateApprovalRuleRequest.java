package com.odoo.plm.dto.request.admin;

import com.odoo.plm.enums.ApprovalCategory;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApprovalRuleRequest {

    @NotNull(message = "Stage ID is required")
    private UUID stageId;

    @NotNull(message = "Approver user ID is required")
    private UUID approverUserId;

    @NotNull(message = "Approval category is required")
    private ApprovalCategory category;
}
