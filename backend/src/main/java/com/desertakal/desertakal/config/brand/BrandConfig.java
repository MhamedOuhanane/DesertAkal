package com.desertakal.desertakal.config.brand;

import com.desertakal.desertakal.service.interfaces.FileStorageService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
@Getter
public class BrandConfig {

    private final FileStorageService fileStorageService;

    private byte[] logoPngBytes;
    private boolean logoLoaded = false;

    public BrandConfig(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostConstruct
    public void init() {
        loadLogo();
    }

//    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void refreshLogo() {
        log.debug("Refreshing brand logo from storage...");
        loadLogo();
    }

    private void loadLogo() {
        try {
            byte[] bytes = fileStorageService.downloadFile(BrandInfo.LOGO_PATH);
            if (bytes != null && bytes.length > 0) {
                this.logoPngBytes = bytes;
                this.logoLoaded = true;
                log.info("Brand logo loaded successfully ({} bytes)", bytes.length);
            } else {
                log.warn("Brand logo not found at: {}", BrandInfo.LOGO_PATH);
                this.logoLoaded = false;
            }
        } catch (Exception e) {
            log.error("Failed to load brand logo: {}", e.getMessage());
            this.logoLoaded = false;
        }
    }
}