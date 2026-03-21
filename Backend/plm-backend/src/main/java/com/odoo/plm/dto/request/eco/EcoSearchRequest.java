package com.odoo.plm.dto.request.eco;

import com.odoo.plm.enums.EcoStatus;
import com.odoo.plm.enums.EcoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoSearchRequest {

    private String title;
    private EcoStatus status;
    private EcoType ecoType;
    private UUID productId;
    private UUID bomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
