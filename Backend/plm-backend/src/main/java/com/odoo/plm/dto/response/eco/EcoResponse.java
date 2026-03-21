package com.odoo.plm.dto.response.eco;

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
public class EcoResponse {

    private UUID id;
    private String title;
    private String description;
    private EcoType ecoType;
    private EcoStatus status;
    private Boolean versionUpdate;
    private LocalDate effectiveDate;

    // Product info
    private UUID productId;
    private String productName;
    private Integer productVersion;

    // BOM info (only for BOM type ECOs)
    private UUID bomId;
    private String bomReference;
    private Integer bomVersion;

    // Current stage info
    private UUID currentStageId;
    private String currentStageName;
    private Integer currentStageSequence;
    private Boolean isAtFinalStage;

    // Changes
    private List<EcoBomChangeResponse> bomChanges;
    private List<EcoProductChangeResponse> productChanges;

    // Approvals
    private List<EcoApprovalResponse> approvals;

    // Metadata
    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
