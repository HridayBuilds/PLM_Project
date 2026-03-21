package com.odoo.plm.dto.response.report;

import com.odoo.plm.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomChangeHistoryResponse {

    private UUID bomId;
    private String bomReference;
    private String productName;
    private List<BomVersionChange> changes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomVersionChange {
        private UUID ecoId;
        private String ecoTitle;
        private Integer fromVersion;
        private Integer toVersion;
        private List<ComponentChange> componentChanges;
        private LocalDateTime appliedAt;
        private String appliedByName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentChange {
        private String componentName;
        private BigDecimal oldQuantity;
        private BigDecimal newQuantity;
        private String unit;
        private ChangeType changeType;
    }
}
