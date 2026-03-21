package com.odoo.plm.dto.request.eco;

import com.odoo.plm.enums.EcoType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateEcoRequest {

    @NotBlank(message = "ECO title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "ECO type is required")
    private EcoType ecoType;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    // Only required for BOM type ECOs
    private UUID bomId;

    @NotNull(message = "Version update flag is required")
    private Boolean versionUpdate;

    // Optional - can be set when creating or updated later
    private LocalDate effectiveDate;
}
