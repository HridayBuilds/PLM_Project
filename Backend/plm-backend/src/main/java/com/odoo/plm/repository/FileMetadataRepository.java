package com.odoo.plm.repository;

import com.odoo.plm.entity.FileMetadata;
import com.odoo.plm.enums.StorageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByUploadedById(UUID userId);

    Page<FileMetadata> findByUploadedById(UUID userId, Pageable pageable);

    List<FileMetadata> findByStorageType(StorageType storageType);

    List<FileMetadata> findByFileType(String fileType);
}
