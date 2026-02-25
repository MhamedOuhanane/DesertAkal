package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.FileUploadException;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    private final MinioClient minioClient;

    @Value("${minio.bucketName:desertakal-bucket}")
    private String bucketName;

    @Value("${minio.url:http://localhost:9000}")
    private String baseUrl;

    @Override
    public String uploadDocument(@NonNull MultipartFile file, String folder) {
        try {
            String fileName = UUID.randomUUID().toString();

            String extension = getFileExtension(file.getOriginalFilename());

            String objectName = String.format("%s/%s%s", folder, fileName, extension);

            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File successfully uploaded to: {}", objectName);

            return objectName;
        } catch (Exception e) {
            log.error("MinIO Upload Error: {}", e.getMessage());
            throw new FileUploadException("Erreur lors du stockage du fichier: " + e.getMessage());
        }
    }

    public String uploadBytes(byte[] data, String objectName, String contentType) {
        try {
            ensureBucketExists();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Document successfully uploaded to MinIO: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Failed to upload generated document: {}", e.getMessage());
            throw new FileUploadException("Error saving generated file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(@NonNull String filePath) {
        if (filePath.isEmpty()) return;

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            log.info("File deleted successfully: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to delete file {}: {}", filePath, e.getMessage());
        }
    }

    @Override
    public String getPublicUrl(String filePath, FileType type) {
        if (filePath == null || filePath.isBlank()) {
            return getUrl(getDefaultImage(type));
        }
        return getUrl(filePath);
    }

    private String getDefaultImage(FileType type) {
        return switch (type) {
            case AVATAR -> "defaults/default-avatar.png";
            case PROFILE -> "defaults/default-profile.png";
            case TOUR -> "defaults/default-tour.png";
            case CITY -> "defaults/default-city.png";
            case ARTICLE -> "defaults/default-article.png";
        };
    }

    private String getUrl(String path) {
        return String.format("%s/%s/%s", baseUrl, bucketName, path);
    }

    private void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket '{}' created successfully", bucketName);
        }
    }

    private String getFileExtension(String objetName) {
        return (objetName != null && objetName.contains("."))
                ? objetName.substring(objetName.lastIndexOf(".")) : ".bin";
    }
}
