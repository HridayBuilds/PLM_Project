package com.odoo.plm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eco_attachments", indexes = {
        @Index(name = "idx_eco_attachment_eco_id", columnList = "eco_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eco_id", nullable = false)
    private Eco eco;

    @NotBlank
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @NotBlank
    @Column(name = "file_url", nullable = false, length = 1024)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize; // In bytes

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
