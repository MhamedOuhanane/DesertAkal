package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.enums.FileType;
import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadDocument(@NonNull MultipartFile file, String folder);
    String uploadBytes(byte[] data,@NonNull String objectName,@NonNull String contentType);
    void deleteFile(@NonNull String filePath);
    String getPublicUrl(String filePath, FileType type);
}
