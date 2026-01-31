package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.mapper.TouristMapper;
import com.desertakal.desertakal.repository.TouristRepository;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.desertakal.desertakal.service.interfaces.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TouristServiceImpl implements TouristService {
    private final TouristRepository repository;
    private final TouristMapper mapper;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public TouristDTO find(UUID touristUuid) {
        log.info("Attempting to find tourist with UUID: {}", touristUuid);

        Tourist tourist = repository.findByUuid(touristUuid)
                .orElseThrow(() -> {
                    log.warn("Found failed: Tourist {} not found", touristUuid);
                    return new ResourceNotFoundException("Tourist", "identifier", touristUuid.toString());
                });

        log.debug("Tourist successfully retrieved: {} (UUID: {})", tourist.getEmail(), touristUuid);

        return mapper.toDto(tourist);
    }

    @Override
    @Transactional
    public TouristDTO updateAvatar(@NonNull UUID touristUuid, @NonNull MultipartFile avatar) {
        log.info("Request to update Avatar for tourist: {}", touristUuid);

        Tourist tourist = repository.findByUuid(touristUuid)
                .orElseThrow(() -> {
                    log.warn("Avatar update failed: Tourist {} not found", touristUuid);
                    return new ResourceNotFoundException("Tourist", "identifier", touristUuid.toString());
                });

        if (!avatar.isEmpty() && avatar.getSize() > 0) {
            String newAvatarUrl = fileStorageService.uploadDocument(avatar, "tourists/avatars");

            if (tourist.getAvatarUrl() != null && !tourist.getAvatarUrl().isBlank()) {
                fileStorageService.deleteFile(tourist.getAvatarUrl());
            }
            tourist.setAvatarUrl(newAvatarUrl);
        } else {
            log.warn("Update skipped: Provided avatar file is empty for tourist {}", touristUuid);
        }

        log.info("Tourist {} updated successfully", touristUuid);

        return mapper.toDto(tourist);
    }

    @Override
    @Transactional
    public UserFindDTO update(@NonNull UUID touristUuid, @NonNull TouristUpdateDTO dto) {
        log.info("Starting update process for user with UUID: {}", touristUuid);

        Tourist tourist = repository.findByUuid(touristUuid)
                .orElseThrow(() -> {
                    log.warn("Update failed: Tourist with UUID {} not found", touristUuid);
                    return new ResourceNotFoundException("Tourist", "identifier", touristUuid.toString());
                });

        log.debug("Mapping UpdateDTO to Tourist entity for UUID: {}", touristUuid);

        mapper.updateEntityFromDto(dto, tourist);

        log.info("Tourist with UUID: {} successfully updated", touristUuid);

        return mapper.toDto(tourist);
    }
}
