package com.odoo.plm.dto.request.bom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomOperationRequest {

    @NotBlank(message = "Operation name is required")
    @Size(min = 2, max = 100, message = "Operation name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Work center is required")
    @Size(max = 100, message = "Work center cannot exceed 100 characters")
    private String workCenter;

    @NotNull(message = "Expected duration is required")
    @Positive(message = "Expected duration must be positive")
    private BigDecimal expectedDurationMinutes;

    private Integer sequence;
}
