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
public class AuditLogResponse {

    private UUID id;
    private UUID ecoId;
    private String ecoTitle;
    private UUID actorId;
    private String actorName;
    private String actorLoginId;
    private String action;
    private String affectedRecord;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
}
