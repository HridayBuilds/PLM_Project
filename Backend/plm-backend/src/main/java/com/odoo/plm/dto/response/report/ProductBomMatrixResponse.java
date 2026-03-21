package com.odoo.plm.dto.response.report;

import com.odoo.plm.enums.BomStatus;
import com.odoo.plm.enums.ProductStatus;
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
public class ProductBomMatrixResponse {

    private List<MatrixRow> matrix;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatrixRow {
        private UUID productId;
        private String productName;
        private Integer productVersion;
        private ProductStatus productStatus;
        private List<BomInfo> boms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomInfo {
        private UUID bomId;
        private String reference;
        private Integer version;
        private BomStatus status;
        private Integer componentCount;
        private Integer operationCount;
    }
}
