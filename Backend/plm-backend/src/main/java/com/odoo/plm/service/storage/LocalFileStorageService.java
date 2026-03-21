package com.odoo.plm.service.storage;

import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;

    public LocalFileStorageService(@Value("${app.storage.local.upload-dir:./uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadPath);
            log.info("Local file storage initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Security check for path traversal
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid file path: " + originalFilename);
        }

        // Generate unique filename
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Create year/month subdirectories for organization
        String subDir = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
        Path targetDir = uploadPath.resolve(subDir);
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File stored locally: {}", targetPath);

        // Return relative path from upload directory
        return subDir + "/" + uniqueFilename;
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            Path filePath = uploadPath.resolve(storagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + storagePath);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path filePath = uploadPath.resolve(storagePath).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storagePath, e);
        }
    }

    @Override
    public String getFileUrl(String storagePath) {
        // For local storage, return the API endpoint path
        return "/api/files/download/" + storagePath;
    }

    @Override
    public boolean exists(String storagePath) {
        Path filePath = uploadPath.resolve(storagePath).normalize();
        return Files.exists(filePath);
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
