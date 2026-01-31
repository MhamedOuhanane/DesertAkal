package com.desertakal.desertakal.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class S3Config {
    @Value("${minio.url}")
    private String url;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            log.info("Bucket '{}' created", bucketName);
        } catch (Exception e) {
            log.info("Bucket '{}' already exists", bucketName);
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }

        setupPublicPolicy(client);

        return client;
    }

    private void setupPublicPolicy(MinioClient client) {
        try {
            String config = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {
                                    "AWS": ["*"]
                                },
                                "Action": [
                                    "s3:GetObject"
                                ],
                                "Resource": [
                                    "arn:aws:s3:::%s/*"
                                ]
                            }
                        ]
                    }
                    """.formatted(bucketName);

            client.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(config)
                            .build()
            );

            log.info("Public read policy applied to bucket '{}'", bucketName);
            log.info("MinIO initialization completed successfully!");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO: {}", e.getMessage(), e);
        }
    }
}
