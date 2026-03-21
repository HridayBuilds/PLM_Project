package com.odoo.plm.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface for file storage operations.
 * Implementations can be Local file system or S3.
 */
public interface FileStorageService {

    /**
     * Store a file and return the storage path/key
     * @param file the file to store
     * @return the storage path (local path or S3 key)
     * @throws IOException if storage fails
     */
    String store(MultipartFile file) throws IOException;

    /**
     * Load a file as a Resource for download
     * @param storagePath the storage path returned from store()
     * @return Resource for the file
     */
    Resource loadAsResource(String storagePath);

    /**
     * Delete a file from storage
     * @param storagePath the storage path to delete
     */
    void delete(String storagePath);

    /**
     * Get the URL/path for accessing the file
     * @param storagePath the storage path
     * @return accessible URL or path
     */
    String getFileUrl(String storagePath);

    /**
     * Check if a file exists
     * @param storagePath the storage path to check
     * @return true if file exists
     */
    boolean exists(String storagePath);
}
