package com.odoo.plm.dto.response.eco;

import com.odoo.plm.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoBomOperationChangeResponse {
    private UUID id;
    private ChangeType changeType;
    private UUID bomOperationId;
    private String operationName;
    private String workCenter;
    private Integer expectedDurationMinutes;
    private Integer sequence;
}
