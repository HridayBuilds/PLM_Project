package com.odoo.plm.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderStagesRequest {

    @NotNull(message = "Stage orders are required")
    private List<StageOrder> stageOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageOrder {
        @NotNull(message = "Stage ID is required")
        private UUID stageId;

        @NotNull(message = "New sequence is required")
        @Positive(message = "Sequence must be positive")
        private Integer sequence;
    }
}
