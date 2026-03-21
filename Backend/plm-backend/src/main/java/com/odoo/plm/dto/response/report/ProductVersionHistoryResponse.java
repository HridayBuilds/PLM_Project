package com.odoo.plm.dto.response.report;

import com.odoo.plm.enums.ProductStatus;
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
public class ProductVersionHistoryResponse {

    private String productName;
    private List<VersionDetail> versions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VersionDetail {
        private UUID productId;
        private Integer version;
        private ProductStatus status;
        private BigDecimal salePrice;
        private BigDecimal costPrice;
        private String description;
        private UUID ecoId;          // ECO that created this version
        private String ecoTitle;
        private LocalDateTime createdAt;
        private String createdByName;
    }
}
