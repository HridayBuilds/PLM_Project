package com.odoo.plm.dto.request.eco;

import com.odoo.plm.enums.ChangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoBomOperationChangeRequest {
    @NotNull(message = "Change type is required")
    private ChangeType changeType;
    
    private UUID bomOperationId;
    
    @NotBlank(message = "Operation name is required")
    private String operationName;
    
    private String workCenter;
    
    @NotNull(message = "Duration is required")
    private Integer expectedDurationMinutes;
    
    private Integer sequence;
}
