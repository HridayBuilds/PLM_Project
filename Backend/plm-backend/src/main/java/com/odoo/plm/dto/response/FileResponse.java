package com.odoo.plm.dto.response;

import com.odoo.plm.enums.StorageType;
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
public class FileResponse {
    private UUID id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private StorageType storageType;
    private String fileUrl;
    private UUID uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
}
