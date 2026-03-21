package com.odoo.plm.dto.response.eco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoComparisonResponse {

    private UUID ecoId;
    private String ecoTitle;

    // Original data
    private OriginalData original;

    // Proposed changes
    private ProposedData proposed;

    // Change summary
    private List<ChangeItem> changes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OriginalData {
        private String productName;
        private Integer productVersion;
        private String salePrice;
        private String costPrice;
        private List<ComponentData> components;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedData {
        private String salePrice;
        private String costPrice;
        private List<ComponentData> components;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentData {
        private String name;
        private String quantity;
        private String unit;
    }

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
}
