package com.odoo.plm.dto.request.eco;

import com.odoo.plm.enums.ApprovalDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveEcoRequest {

    @NotNull(message = "Decision is required")
    private ApprovalDecision decision;

    @Size(max = 1000, message = "Comments cannot exceed 1000 characters")
    private String comments;
}
