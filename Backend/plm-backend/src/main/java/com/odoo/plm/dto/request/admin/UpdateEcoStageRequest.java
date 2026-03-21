package com.odoo.plm.dto.request.admin;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEcoStageRequest {

    @Size(min = 2, max = 100, message = "Stage name must be between 2 and 100 characters")
    private String name;

    @Positive(message = "Sequence must be positive")
    private Integer sequence;

    private Boolean isFinal;
}
