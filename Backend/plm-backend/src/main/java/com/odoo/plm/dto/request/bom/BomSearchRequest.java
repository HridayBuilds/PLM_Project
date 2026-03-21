package com.odoo.plm.dto.request.bom;

import com.odoo.plm.enums.BomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomSearchRequest {

    private String reference;
    private BomStatus status;
    private UUID productId;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
