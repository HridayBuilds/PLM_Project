package com.odoo.plm.dto.response.eco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoStageResponse {

    private UUID id;
    private String name;
    private Integer sequence;
    private Boolean isFinal;
    private Integer approverCount;
    private Integer requiredApproverCount;
}
