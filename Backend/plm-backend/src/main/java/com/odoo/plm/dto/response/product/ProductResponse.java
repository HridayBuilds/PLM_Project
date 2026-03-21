package com.odoo.plm.dto.response.product;

import com.odoo.plm.dto.response.FileResponse;
import com.odoo.plm.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer version;
    private ProductStatus status;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private List<FileResponse> attachments;
    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
