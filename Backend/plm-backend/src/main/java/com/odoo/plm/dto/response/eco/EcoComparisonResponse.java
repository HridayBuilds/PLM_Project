package com.odoo.plm.dto.response.eco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoComparisonResponse {

    private UUID ecoId;
    private String ecoTitle;
    private String type; // "Product" or "BOM"

    // For Product Type
    private List<ChangeItem> changes;

    // For BOM Type
    private List<ComponentComparison> components;
    private List<OperationComparison> operations;
    private ComparisonSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeItem {
        private String field;
        private String oldValue;
        private String newValue;
        private String changeType; // MODIFIED, ADDED, REMOVED
        private String color; // green, red, black for UI
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentComparison {
        private UUID id;
        private String name;
        private BigDecimal version1Qty;
        private BigDecimal version2Qty;
        private String unit;
        private String changeType; // ADD, REMOVE, UPDATE, UNCHANGED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationComparison {
        private UUID id;
        private String name;
        private String workCenter;
        private Integer version1Duration;
        private Integer version2Duration;
        private String changeType; // ADD, REMOVE, UPDATE, UNCHANGED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonSummary {
        private BigDecimal totalPartsDelta;
        private Integer productionCycleDelta;
        private BigDecimal estimatedCostImpact;
    }
}
