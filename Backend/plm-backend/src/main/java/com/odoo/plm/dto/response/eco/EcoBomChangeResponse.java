package com.odoo.plm.dto.response.eco;

import com.odoo.plm.enums.ChangeType;
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
public class EcoBomChangeResponse {

    private UUID id;
    private UUID bomComponentId;
    private String componentName;
    private BigDecimal oldQuantity;
    private BigDecimal newQuantity;
    private String unit;
    private ChangeType changeType;
}
