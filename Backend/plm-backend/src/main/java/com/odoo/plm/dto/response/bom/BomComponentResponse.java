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
public class BomComponentResponse {

    private UUID id;
    private UUID componentProductId;
    private String componentProductName;
    private BigDecimal quantity;
    private String unit;
}
