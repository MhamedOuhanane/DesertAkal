package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.cityTour.CityTourCreateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import com.desertakal.desertakal.model.entity.City;
import com.desertakal.desertakal.model.entity.CityTour;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.model.mapper.CityTourMapper;
import com.desertakal.desertakal.model.mapper.TourMapper;
import com.desertakal.desertakal.repository.CityRepository;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.desertakal.desertakal.service.interfaces.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TourServiceImpl implements TourService {
    private final TourRepository repository;
    private final TourMapper mapper;
    private final FileStorageService fileStorageService;
    private final CityRepository cityRepository;
    private final CityTourMapper cityTourMapper;

    @Override
    @Transactional
    public TourFindDTO create(@NonNull TourCreateDTO dto, @NonNull MultipartFile image) {
        log.info("Starting creation of Tour: '{}'", dto.getTitle());

        if (repository.existsByTitle(dto.getTitle())) {
            log.error("Validation failed: Tour title '{}' is a duplicate", dto.getTitle());
            throw new DuplicateResourceException("Tour", "title", dto.getTitle());
        }

        Tour tour = mapper.toEntity(dto);
        List<CityTour> cityTours = mapCityTours(dto.getCityTours(), tour);
        tour.setCityTours(cityTours);

        int totalDays = cityTours.stream().mapToInt(CityTour::getDaysCount).sum();
        tour.setDurationDays(totalDays);
        log.debug("Calculated total duration: {} days across {} cities", totalDays, cityTours.size());

        String imagePath = fileStorageService.uploadDocument(image, "tours");
        tour.setImage(imagePath);
        log.info("Cover image uploaded to path: {}", imagePath);

        Tour newTour = repository.save(tour);

        log.info("Tour successfully created. Assigned UUID: {} [Title: '{}']",
                newTour.getUuid(), newTour.getTitle());

        return mapper.toFindDto(newTour);
    }

    @Override
    public TourFindDTO update(@NonNull UUID tourUuid, @NonNull TourUpdateDTO dto) {
        return null;
    }

    @Override
    public TourFindDTO updateImage(@NonNull UUID tourUuid, @NonNull MultipartFile image) {
        return null;
    }

    @Override
    public TourFindDTO find(@NonNull UUID tourUuid) {
        log.info("Attempting to find Tour with UUID: {}", tourUuid);

        Tour tour = repository.findByUuid(tourUuid)
                .orElseThrow(() -> {
                    log.warn("Found failed: Tour {} not found", tourUuid);
                    return new ResourceNotFoundException("Tour", "identifier", tourUuid.toString());
                });

        log.debug("Tour successfully retrieved: {} (UUID: {})", tour.getTitle(), tourUuid);

        return mapper.toFindDto(tour);
    }

    @Override
    public PaginationDTO findAll(String search, String city, String durationStr, Double minRating, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public void delete(@NonNull UUID tourUuid) {

    }

    @Override
    public List<TourDTO> findTop5() {
        return List.of();
    }

    @Override
    public PaginationDTO findAllByTourist(@NonNull UUID touristUuid, @NonNull Pageable pageable) {
        return null;
    }

    @Override
    public PaginationDTO findAllByGuide(@NonNull UUID guideUuid, @NonNull Pageable pageable) {
        return null;
    }

    private List<CityTour> mapCityTours(@NonNull List<CityTourCreateDTO> DTOs, Tour tour) {
        log.debug("Mapping {} CityTourDTOs for tour: '{}'", DTOs.size(), tour.getTitle());

        return DTOs.stream()
                .map(ctDto -> {
                    City city = cityRepository.findByUuid(ctDto.getCityUuid())
                            .orElseThrow(() -> {
                                log.error("City mapping failed: UUID {} not found", ctDto.getCityUuid());
                                return new ResourceNotFoundException("City", "uuid", ctDto.getCityUuid().toString());
                            });

                    CityTour cityTour = cityTourMapper.toEntity(ctDto);
                    cityTour.setCity(city);
                    cityTour.setTour(tour);

                    log.debug("Mapped city: '{}' with order: {} and days: {}",
                            city.getName(), cityTour.getOrderIndex(), cityTour.getDaysCount());

                    return cityTour;
                }).toList();
    }
}
