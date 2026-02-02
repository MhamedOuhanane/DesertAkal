package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityDTO;
import com.desertakal.desertakal.model.dto.city.CityFIndDTO;
import com.desertakal.desertakal.model.dto.city.CityUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.City;
import com.desertakal.desertakal.model.mapper.CityMapper;
import com.desertakal.desertakal.repository.CityRepository;
import com.desertakal.desertakal.service.interfaces.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {
    private final CityRepository repository;
    private final CityMapper mapper;

    @Override
    public CityFIndDTO create(@NonNull CityCreateDTO dto) {
        log.info("Starting creation of new City: '{}' ", dto.getName());

        if (repository.existsByName(dto.getName())) {
            log.warn("Create failed: City name '{}' already exists", dto.getName());
            throw new DuplicateResourceException("City", "name", dto.getName());
        }

        City role = mapper.toEntity(dto);

        City savedCity = repository.save(role);

        log.info("Successfully created City: '{}' with UUID: {}", savedCity.getName(), savedCity.getUuid());

        return mapper.toFindDto(savedCity);
    }

    @Override
    public CityFIndDTO find(@NonNull UUID cityUuid) {
        return null;
    }

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public List<CityDTO> findByTour(@NonNull UUID tourUuid) {
        return List.of();
    }

    @Override
    public CityFIndDTO update(@NonNull UUID cityUuid, @NonNull CityUpdateDTO dto) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID cityUuid) {

    }

    @Override
    public CityFIndDTO addImages(@NonNull UUID cityUuid, @NonNull List<MultipartFile> images) {
        return null;
    }

    @Override
    public void deleteImage(@NonNull UUID cityUuid, @NonNull UUID imageUuid) {

    }

    @Override
    public void setCoverImage(@NonNull UUID cityUuid, @NonNull UUID imageUuid) {

    }
}
