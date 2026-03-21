package com.odoo.plm.service.storage;

import com.odoo.plm.exception.BadRequestException;
import com.odoo.plm.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
@Slf4j
public class S3FileStorageService implements FileStorageService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        log.info("S3 file storage initialized for bucket: {}", bucketName);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Security check
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid file path: " + originalFilename);
        }

        // Generate unique S3 key
        String extension = getFileExtension(originalFilename);
        String subDir = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
        String s3Key = "plm-files/" + subDir + "/" + UUID.randomUUID().toString() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File stored in S3: {}", s3Key);

            return s3Key;
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            return new InputStreamResource(s3Client.getObject(getRequest));
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("File not found in S3: " + storagePath);
        } catch (S3Exception e) {
            log.error("Failed to retrieve file from S3", e);
            throw new ResourceNotFoundException("Failed to retrieve file: " + storagePath);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from S3: {}", storagePath);
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", storagePath, e);
        }
    }

    @Override
    public String getFileUrl(String storagePath) {
        // Return the S3 URL
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, storagePath);
    }

    @Override
    public boolean exists(String storagePath) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
