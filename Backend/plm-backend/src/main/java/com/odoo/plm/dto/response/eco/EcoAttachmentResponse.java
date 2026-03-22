package com.odoo.plm.dto.response.eco;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoAttachmentResponse {
    private UUID id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime createdAt;
}
