package com.odoo.plm.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBomMatrixResponse {
    private UUID productId;
    private String productName;
    private Integer activeVersion;
    private Integer activeBomVersion;
    private Integer componentsCount;
    private LocalDateTime lastChangeDate;
}
