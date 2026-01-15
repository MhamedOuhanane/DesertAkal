package com.desertakal.desertakal.service.interfaces;

import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadDocument(@NonNull MultipartFile file, String folder);
    void deleteFile(@NonNull String filePath);
    String getPublicUrl(@NonNull String filePath);
}
