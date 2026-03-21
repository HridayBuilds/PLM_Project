package com.odoo.plm.dto.request.eco;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoBomOperationChangeRequest {

    // For modifying operations in BOM ECO
    @Valid
    private List<BomOperationChangeItem> operations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomOperationChangeItem {
        private String name;
        private String workCenter;
        private Integer expectedDurationMinutes;
        private Integer sequence;
        private String changeType; // ADDED, MODIFIED, REMOVED
    }
}
