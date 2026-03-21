package com.odoo.plm.service;

import com.odoo.plm.dto.response.FileResponse;
import com.odoo.plm.entity.FileMetadata;
import com.odoo.plm.entity.User;
import com.odoo.plm.enums.StorageType;
import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.ResourceNotFoundException;
import com.odoo.plm.exception.UnauthorizedException;
import com.odoo.plm.repository.FileMetadataRepository;
import com.odoo.plm.repository.UserRepository;
import com.odoo.plm.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileStorageService fileStorageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;

    @Value("${app.storage.type:local}")
    private String storageType;

    @Value("${app.storage.allowed-types:pdf,png,jpg,jpeg,doc,docx,xls,xlsx}")
    private String allowedTypes;

    @Value("${app.storage.max-file-size:10485760}") // 10MB default
    private Long maxFileSize;

    @Transactional
    public FileResponse uploadFile(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Get current user
        User currentUser = getCurrentUser();

        // Store file
        String storagePath = fileStorageService.store(file);

        // Save metadata
        FileMetadata metadata = FileMetadata.builder()
                .fileName(extractFileName(storagePath))
                .originalFileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .storageType(StorageType.valueOf(storageType.toUpperCase()))
                .storagePath(storagePath)
                .uploadedBy(currentUser)
                .build();

        metadata = fileMetadataRepository.save(metadata);

        log.info("File uploaded: {} by user: {}", metadata.getId(), currentUser.getLoginId());

        return mapToResponse(metadata);
    }

    @Transactional(readOnly = true)
    public FileResponse getFileInfo(UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
        return mapToResponse(metadata);
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
        return fileStorageService.loadAsResource(metadata.getStoragePath());
    }

    @Transactional(readOnly = true)
    public Resource downloadFileByPath(String storagePath) {
        return fileStorageService.loadAsResource(storagePath);
    }

    @Transactional
    public void deleteFile(UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        User currentUser = getCurrentUser();

        // Only uploader or admin can delete
        if (!metadata.getUploadedBy().getId().equals(currentUser.getId())
                && !currentUser.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("You don't have permission to delete this file");
        }

        // Delete from storage
        fileStorageService.delete(metadata.getStoragePath());

        // Delete metadata
        fileMetadataRepository.delete(metadata);

        log.info("File deleted: {} by user: {}", fileId, currentUser.getLoginId());
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> getMyFiles(Pageable pageable) {
        User currentUser = getCurrentUser();
        return fileMetadataRepository.findByUploadedById(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFilesByIds(List<UUID> fileIds) {
        return fileMetadataRepository.findAllById(fileIds).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BadRequestException("File name is required");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));

        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedTypes);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    private String extractFileName(String storagePath) {
        int lastSlash = storagePath.lastIndexOf('/');
        return lastSlash >= 0 ? storagePath.substring(lastSlash + 1) : storagePath;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginId = authentication.getName();
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private FileResponse mapToResponse(FileMetadata metadata) {
        return FileResponse.builder()
                .id(metadata.getId())
                .fileName(metadata.getFileName())
                .originalFileName(metadata.getOriginalFileName())
                .fileType(metadata.getFileType())
                .fileSize(metadata.getFileSize())
                .storageType(metadata.getStorageType())
                .fileUrl(fileStorageService.getFileUrl(metadata.getStoragePath()))
                .uploadedById(metadata.getUploadedBy().getId())
                .uploadedByName(metadata.getUploadedBy().getFirstName() + " " + metadata.getUploadedBy().getLastName())
                .createdAt(metadata.getCreatedAt())
                .build();
    }
}
