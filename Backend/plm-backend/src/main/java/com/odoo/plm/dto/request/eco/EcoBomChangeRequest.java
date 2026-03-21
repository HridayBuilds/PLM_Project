package com.odoo.plm.dto.request.eco;

import com.odoo.plm.enums.ChangeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class EcoBomChangeRequest {

    // For MODIFIED or REMOVED - specify existing component
    private UUID bomComponentId;

    // For ADDED - specify new component product
    private UUID componentProductId;

    // Required for ADDED and MODIFIED
    @Positive(message = "New quantity must be positive")
    private BigDecimal newQuantity;

    // Required for ADDED
    private String unit;

    @NotNull(message = "Change type is required")
    private ChangeType changeType;
}
