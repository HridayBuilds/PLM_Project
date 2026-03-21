package com.odoo.plm.dto.response.bom;

import com.odoo.plm.enums.BomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private String reference;
    private Integer version;
    private BomStatus status;
    private List<BomComponentResponse> components;
    private List<BomOperationResponse> operations;
    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
