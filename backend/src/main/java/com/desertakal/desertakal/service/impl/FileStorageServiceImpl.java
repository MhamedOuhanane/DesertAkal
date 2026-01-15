package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.FileUploadException;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public void deleteFile(@NonNull String filePath) {

    }

    @Override
    public String getPublicUrl(@NonNull String filePath) {
        return "";
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
