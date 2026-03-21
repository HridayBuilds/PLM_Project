package com.odoo.plm.dto.response.bom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomOperationResponse {

    private UUID id;
    private String name;
    private String workCenter;
    private BigDecimal expectedDurationMinutes;
    private Integer sequence;
}
