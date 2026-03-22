package com.odoo.plm.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedProductResponse {

    private UUID productId;
    private String name;
    private String description;
    private Integer version;
    private BigDecimal salePrice;
    private BigDecimal costPrice;

    // Archive metadata
    private LocalDateTime archivedAt;
    private String archivedByName;

    // Reference to replacement product (if any)
    private UUID replacementProductId;
    private Integer replacementVersion;

    // ECO that caused archival
    private UUID ecoId;
    private String ecoTitle;

    // Original creation info
    private String createdByName;
    private LocalDateTime createdAt;
}
