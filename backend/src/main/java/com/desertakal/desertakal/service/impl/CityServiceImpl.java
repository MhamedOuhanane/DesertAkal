package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
import com.desertakal.desertakal.exception.custom.ResourceMismatchException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityDTO;
import com.desertakal.desertakal.model.dto.city.CityFIndDTO;
import com.desertakal.desertakal.model.dto.city.CityUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.City;
import com.desertakal.desertakal.model.entity.Image;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.mapper.CityMapper;
import com.desertakal.desertakal.repository.CityRepository;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.service.interfaces.CityService;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {
    private final CityRepository repository;
    private final CityMapper mapper;
    private final FileStorageService fileStorageService;
    private final TourRepository tourRepository;

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
    @Transactional(readOnly = true)
    public CityFIndDTO find(@NonNull UUID cityUuid) {
        log.info("Attempting to find City with UUID: {}", cityUuid);

        City city = repository.findByUuid(cityUuid)
                .orElseThrow(() -> {
                    log.warn("Found failed: City {} not found", cityUuid);
                    return new ResourceNotFoundException("City", "identifier", cityUuid.toString());
                });

        log.debug("City successfully retrieved: {} (UUID: {})", city.getName(), cityUuid);

        return mapper.toFindDto(city);
    }

    @Override
    public PaginationDTO findAll(String search, @NonNull Pageable pageable) {
        log.info("Fetching cities : [Search: '{}', Page: {}]",
                search != null ? search : "NONE", pageable.getPageNumber());

        Specification<@NonNull City> spec = (root, query, cb) -> {
            if (search != null && !search.isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                return cb.like(cb.lower(root.get("name")), pattern);
            }

            return cb.conjunction();
        };

        var cityPages = repository.findAll(spec, pageable);

        log.info("Success: Retrieved {} cities (Total elements in DB: {})",
                cityPages.getNumberOfElements(),
                cityPages.getTotalElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(cityPages.getContent()))
                .page(cityPages.getNumber())
                .size(cityPages.getSize())
                .totalElements(cityPages.getTotalElements())
                .totalPages(cityPages.getTotalPages())
                .isFirst(cityPages.isFirst())
                .isLast(cityPages.isLast())
                .build();
    }

    @Override
    public List<CityDTO> findByTour(@NonNull UUID tourUuid) {

        Tour tour = tourRepository.findByUuid(tourUuid)
                .orElseThrow(() -> {
                    log.warn("Found failed: Tour {} not found", tourUuid);
                    return new ResourceNotFoundException("Tour", "identifier", tourUuid.toString());
                });

        var cites = repository.findByCityToursTour(tour);

        log.info("Success: Retrieved {} cities", cites.size());

        return mapper.toDtos(cites);
    }

    @Override
    public CityFIndDTO update(@NonNull UUID cityUuid, @NonNull CityUpdateDTO dto) {
        log.info("Starting update process for City with UUID: {}", cityUuid);

        City city = repository.findByUuid(cityUuid)
                .orElseThrow(() -> {
                    log.warn("Update failed: City with UUID {} not found", cityUuid);
                    return new ResourceNotFoundException("City", "identifier", cityUuid.toString());
                });

        log.debug("Mapping UpdateDTO to City entity for UUID: {}", cityUuid);

        mapper.updateEntityFromDto(dto, city);

        log.info("City with UUID: {} successfully updated", cityUuid);

        return mapper.toFindDto(city);
    }

    @Override
    public void delete(@NonNull UUID cityUuid) {

    }

    @Override
    @Transactional
    public CityFIndDTO addImages(@NonNull UUID cityUuid, @NonNull List<MultipartFile> images) {
        log.info("Starting to add {} images to city with UUID: {}", images.size(), cityUuid);

        City city = repository.findByUuid(cityUuid)
                .orElseThrow(() -> {
                    log.error("Add images failed: City not found with UUID: {}", cityUuid);
                    return new ResourceNotFoundException("City", "identifier", cityUuid.toString());
                });

        boolean hasCover = city.getImages().stream().anyMatch(Image::getIsCover);
        log.debug("Current city cover status: {}", hasCover ? "Already has a cover" : "No cover found, setting first new image as cover");

        List<Image> imagesEntity = IntStream.range(0, images.size())
                .mapToObj(i -> {
                    String path = fileStorageService.uploadDocument(images.get(i), "cities/");
                    log.debug("Image {} uploaded successfully to path: {}", i + 1, path);

                    return Image.builder()
                            .image(path)
                            .isCover(!hasCover && i == 0)
                            .build();
                }).toList();

        city.getImages().addAll(imagesEntity);
        log.info("Successfully linked {} new images to city: {}", imagesEntity.size(), city.getName());

        return mapper.toFindDto(city);
    }

    @Override
    @Transactional
    public void deleteImage(@NonNull UUID cityUuid, @NonNull List<UUID> imageUuids) {
        log.info("Starting to delete {} images to city with UUID: {}", imageUuids.size(), cityUuid);

        City city = repository.findByUuid(cityUuid)
                .orElseThrow(() -> {
                    log.error("Delete images failed: City not found with UUID: {}", cityUuid);
                    return new ResourceNotFoundException("City", "identifier", cityUuid.toString());
                });

        List<Image> imagesToDelete = city.getImages().stream()
                .filter(img -> imageUuids.contains(img.getUuid()))
                .toList();

        if (imagesToDelete.isEmpty()) {
            log.warn("No matching images found for the provided UUIDs in City: {}", city.getName());
            return;
        }

        boolean deletingCover = imagesToDelete.stream().anyMatch(Image::getIsCover);

        city.getImages().removeAll(imagesToDelete);

        imagesToDelete.forEach(image -> {
            fileStorageService.deleteFile(image.getImage());
            log.debug("Physical file deleted: {}", image.getImage());
        });

        if (deletingCover && !city.getImages().isEmpty()) {
            city.getImages().get(0).setIsCover(true);
            log.info("Current cover was deleted. Image [UUID: {}] is now the new cover for city: {}",
                    city.getImages().get(0).getUuid(), city.getName());
        }

        log.info("Successfully deleted {} images from city: {}", imagesToDelete.size(), city.getName());
    }

    @Override
    @Transactional
    public void setCoverImage(@NonNull UUID cityUuid, @NonNull UUID imageUuid) {
        log.info("Request to set image [{}] as cover for city [{}]", imageUuid, cityUuid);

        City city = repository.findByUuid(cityUuid)
                .orElseThrow(() -> {
                    log.error("Set cover failed: City not found with UUID: {}", cityUuid);
                    return new ResourceNotFoundException("City", "identifier", cityUuid.toString());
                });

        boolean imageFound = false;
        for (Image img : city.getImages()) {
            if (img.getUuid().equals(imageUuid)) {
                img.setIsCover(true);
                imageFound = true;
                log.debug("Image [{}] found and set as cover", imageUuid);
            } else {
                img.setIsCover(false);
            }
        }

        if (!imageFound) {
            log.warn("Target image [{}] not found in city [{}]", imageUuid, cityUuid);
            throw new ResourceMismatchException("Image", "City", imageUuid.toString());
        }

        log.info("Successfully updated cover image for city: {}", city.getName());
    }
}
