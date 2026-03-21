package com.odoo.plm.dto.response.report;

import com.odoo.plm.dto.response.eco.EcoApprovalResponse;
import com.odoo.plm.dto.response.eco.EcoBomChangeResponse;
import com.odoo.plm.dto.response.eco.EcoProductChangeResponse;
import com.odoo.plm.enums.EcoStatus;
import com.odoo.plm.enums.EcoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoReportResponse {

    private UUID ecoId;
    private String title;
    private String description;
    private EcoType ecoType;
    private EcoStatus status;
    private Boolean versionUpdate;
    private LocalDate effectiveDate;

    // Product info
    private String productName;
    private Integer originalProductVersion;
    private Integer newProductVersion;

    // BOM info (if applicable)
    private String bomReference;
    private Integer originalBomVersion;
    private Integer newBomVersion;

    // Changes
    private List<EcoBomChangeResponse> bomChanges;
    private List<EcoProductChangeResponse> productChanges;

    // Approval history
    private List<EcoApprovalResponse> approvalHistory;

    // Audit trail
    private List<AuditLogResponse> auditTrail;

    // Metadata
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
