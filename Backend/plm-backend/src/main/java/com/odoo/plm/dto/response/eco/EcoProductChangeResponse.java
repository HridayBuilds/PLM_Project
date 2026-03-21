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
public class EcoProductChangeResponse {

    private UUID id;
    private String fieldName;
    private String oldValue;
    private String newValue;
}
