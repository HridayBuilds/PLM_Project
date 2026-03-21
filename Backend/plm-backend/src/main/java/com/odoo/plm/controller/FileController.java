package com.odoo.plm.controller;

import com.odoo.plm.dto.response.FileResponse;
import com.odoo.plm.dto.response.MessageResponse;
import com.odoo.plm.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for file upload, download, and management")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file", description = "Upload a file to storage (local or S3 based on configuration)")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        FileResponse response = fileService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file info", description = "Get metadata information about a file")
    public ResponseEntity<FileResponse> getFileInfo(@PathVariable UUID id) {
        FileResponse response = fileService.getFileInfo(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a file", description = "Download a file by its ID")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        FileResponse fileInfo = fileService.getFileInfo(id);
        Resource resource = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/download/{year}/{month}/{filename}")
    @Operation(summary = "Download file by path", description = "Download a file using its storage path")
    public ResponseEntity<Resource> downloadFileByPath(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename) {
        String storagePath = year + "/" + month + "/" + filename;
        Resource resource = fileService.downloadFileByPath(storagePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file", description = "Delete a file (only uploader or admin can delete)")
    public ResponseEntity<MessageResponse> deleteFile(@PathVariable UUID id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok(new MessageResponse("File deleted successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my files", description = "Get files uploaded by the current user")
    public ResponseEntity<Page<FileResponse>> getMyFiles(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<FileResponse> files = fileService.getMyFiles(pageable);
        return ResponseEntity.ok(files);
    }
}
