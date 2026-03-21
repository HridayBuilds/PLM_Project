package com.odoo.plm.dto.request.eco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcoProductChangeRequest {

    @NotBlank(message = "Field name is required")
    @Size(max = 50, message = "Field name cannot exceed 50 characters")
    private String fieldName;

    @NotBlank(message = "New value is required")
    @Size(max = 500, message = "New value cannot exceed 500 characters")
    private String newValue;
}
