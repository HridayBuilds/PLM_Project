package com.odoo.plm.dto.request.bom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBomRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "BOM reference is required")
    @Size(min = 2, max = 100, message = "Reference must be between 2 and 100 characters")
    private String reference;

    @Valid
    private List<BomComponentRequest> components;

    @Valid
    private List<BomOperationRequest> operations;
}
