package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.DuplicateResourceException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.cityTour.CityTourCreateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import com.desertakal.desertakal.model.entity.*;
import com.desertakal.desertakal.model.mapper.CityTourMapper;
import com.desertakal.desertakal.model.mapper.TourMapper;
import com.desertakal.desertakal.repository.CityRepository;
import com.desertakal.desertakal.repository.GuideRepository;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.repository.TouristRepository;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.desertakal.desertakal.service.interfaces.TourService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final TouristRepository touristRepository;
    private final GuideRepository guideRepository;

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
    @Transactional
    public TourFindDTO update(@NonNull UUID tourUuid, @NonNull TourUpdateDTO dto) {
        log.info("Starting update process for Tour with UUID: {}", tourUuid);

        Tour tour = repository.findByUuid(tourUuid)
                .orElseThrow(() -> {
                    log.warn("Update failed: Tour with UUID {} not found", tourUuid);
                    return new ResourceNotFoundException("Tour", "identifier", tourUuid.toString());
                });

        log.debug("Mapping UpdateDTO to Tour entity for UUID: {}", tourUuid);

        mapper.updateEntityFromDto(dto, tour);

        if (dto.getCityTours() != null && !dto.getCityTours().isEmpty()) {
                log.info("Updating CityTours list for Tour: {}. New count: {}", tourUuid, dto.getCityTours().size());

                tour.getCityTours().clear();
                repository.saveAndFlush(tour);

                List<CityTour> newCityTours = mapCityTours(dto.getCityTours(), tour);
                tour.getCityTours().addAll(newCityTours);

                int totalDays = newCityTours.stream().mapToInt(CityTour::getDaysCount).sum();
                tour.setDurationDays(totalDays);
                log.debug("New duration calculated: {} days", totalDays);
            }

        Tour updatedTour = repository.save(tour);
        log.info("Tour [UUID: {}] successfully persisted in database", tourUuid);

        return mapper.toFindDto(updatedTour);
    }

    @Override
    @Transactional
    public TourFindDTO updateImage(@NonNull UUID tourUuid, @NonNull MultipartFile image) {
        log.info("Request to update image for Tour UUID: {}", tourUuid);

        Tour tour = repository.findByUuid(tourUuid)
                .orElseThrow(() -> {
                    log.error("Update Image abort: Tour with UUID {} not found", tourUuid);
                    return new ResourceNotFoundException("Tour", "identifier", tourUuid.toString());
                });

        if (image.isEmpty()) {
            log.warn("Update Image skipped: The provided multipart file is empty for Tour {}", tourUuid);
            return mapper.toFindDto(tour);
        }

        log.debug("Processing new image: Name={}, Size={} bytes", image.getOriginalFilename(), image.getSize());

        String newImagePath = fileStorageService.uploadDocument(image, "tours");
        log.info("New image uploaded successfully to: {}", newImagePath);

        String oldImagePath = tour.getImage();
        if (oldImagePath != null && !oldImagePath.isBlank() && !oldImagePath.contains("defaults/")) {
            fileStorageService.deleteFile(oldImagePath);
        }

        tour.setImage(newImagePath);
        Tour updatedTour = repository.save(tour);

        log.info("Tour image path updated in database for UUID: {}", tourUuid);

        return mapper.toFindDto(updatedTour);
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
    public PaginationDTO findAll(String search, String city, String durationStr, BigDecimal minRating, @NonNull Pageable pageable) {
        log.info("Fetching tours with filters - Search: '{}', City: '{}', Duration: '{}', MinRating: {}",
                search, city, durationStr, minRating);

        Specification<@NonNull Tour> spec = getToursSpecification(search, city, durationStr, minRating);

        var tourPage = repository.findAll(spec, pageable);

        log.info("Search completed: Found {} items on page {} of {}",
                tourPage.getNumberOfElements(), tourPage.getNumber(), tourPage.getTotalPages());

        return PaginationDTO.builder()
                .content(mapper.toDtos(tourPage.getContent()))
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .totalElements(tourPage.getTotalElements())
                .totalPages(tourPage.getTotalPages())
                .isFirst(tourPage.isFirst())
                .isLast(tourPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID tourUuid) {
        log.info("Starting deletion process for tour with UUID: {}", tourUuid);

        Tour tour = repository.findByUuid(tourUuid)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Tour with UUID {} not found", tourUuid);
                    return new ResourceNotFoundException("Tour", "identifier", tourUuid.toString());
                });

        if (!tour.getReservations().isEmpty()) {
            int reservationCount = tour.getReservations().size();
            log.warn("Constraint Violation: Attempt to delete Tour [UUID: {}, Title: '{}'] denied. Active reservations count: {}",
                    tour.getUuid(), tour.getTitle(), reservationCount);

            throw new BusinessRuleException(
                    String.format("Action Refused: The tour '%s' is protected because it has %d associated reservation(s). " +
                                    "To protect data integrity, you must process these reservations before deleting the tour.",
                            tour.getTitle(), reservationCount)
            );
        }

        log.warn("Tour identified for deletion - Title: {}, UUID: {}, Creating at: {}",
                tour.getTitle(), tour.getUuid(), tour.getCreatedAt());

        String imagePath = tour.getImage();

        repository.delete(tour);

        fileStorageService.deleteFile(imagePath);

        log.info("Tour with UUID: {} and Title: {} successfully deleted from system",
                tourUuid, tour.getTitle());
    }

    @Override
    public List<TourDTO> findTop5() {
        log.debug("Fetching Top 5 highest rated tours");

        List<Tour> toursTop5 = repository.findTop5ByOrderByRatingDesc();

        if (toursTop5.isEmpty()) {
            log.warn("No tours found for Top 5 list");
        }

        return mapper.toDtos(toursTop5);
    }

    @Override
    public PaginationDTO findAllByTourist(@NonNull UUID touristUuid, @NonNull Pageable pageable) {
        log.info("Starting process to fetch tours for Tourist UUID: {} [Page: {}, Size: {}]",
                touristUuid, pageable.getPageNumber(), pageable.getPageSize());

        Tourist tourist = touristRepository.findByUuid(touristUuid)
                .orElseThrow(() -> {
                    log.error("Fetch Tours failed: Tourist with UUID {} not found", touristUuid);
                    return new ResourceNotFoundException("Tourist", "identifier", touristUuid.toString());
                });

        log.debug("Tourist found: {} {}. Executing join query for tours.",
                tourist.getFirstName(), tourist.getLastName());

        Page<@NonNull Tour> tourPage = repository.findAllByTourist(tourist, pageable);

        log.info("Search completed: Found {} tours for tourist {} on page {} of {}",
                tourPage.getTotalElements(), touristUuid, tourPage.getNumber(), tourPage.getTotalPages());

        return PaginationDTO.builder()
                .content(mapper.toDtos(tourPage.getContent()))
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .totalElements(tourPage.getTotalElements())
                .totalPages(tourPage.getTotalPages())
                .isFirst(tourPage.isFirst())
                .isLast(tourPage.isLast())
                .build();
    }

    @Override
    public PaginationDTO findAllByGuide(@NonNull UUID guideUuid, @NonNull Pageable pageable) {
        log.info("Starting process to fetch tours for Guide UUID: {} [Page: {}, Size: {}]",
                guideUuid, pageable.getPageNumber(), pageable.getPageSize());

        Guide guide = guideRepository.findByUuid(guideUuid)
                .orElseThrow(() -> {
                    log.error("Fetch Tours failed: Guide with UUID {} not found", guideUuid);
                    return new ResourceNotFoundException("Guide", "identifier", guideUuid.toString());
                });

        log.debug("Guide found: {} {}. Executing join query for tours.",
                guide.getFirstName(), guide.getLastName());

        Page<@NonNull Tour> tourPage = repository.findAllByGuide(guide, pageable);

        log.info("Search completed: Found {} tours for guide {} on page {} of {}",
                tourPage.getTotalElements(), guideUuid, tourPage.getNumber(), tourPage.getTotalPages());

        return PaginationDTO.builder()
                .content(mapper.toDtos(tourPage.getContent()))
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .totalElements(tourPage.getTotalElements())
                .totalPages(tourPage.getTotalPages())
                .isFirst(tourPage.isFirst())
                .isLast(tourPage.isLast())
                .build();
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

    private Specification<@NonNull Tour> getToursSpecification(String search, String city, String durationStr, BigDecimal minRating) {
        return  (root, query, cb) -> {
            query.distinct(true);

            log.debug("Building Specification predicates for Tour search...");

            List<Predicate> predicates = new ArrayList<>();
            Join<Object, Object> cityJoin = root.join("cityTours", JoinType.LEFT).join("city", JoinType.LEFT);

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
                log.debug("Filter added: title LIKE '%{}%'", search);
            }

            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(cb.lower(cityJoin.get("name")), "%" + city.toLowerCase() + "%"));
                log.debug("Filter added: city.name LIKE '%{}%'", city);
            }

            if (durationStr != null && !durationStr.isBlank()) {
                String cleanDuration = durationStr.trim();
                try {
                    if (cleanDuration.startsWith("+")) {
                        int val = Integer.parseInt(cleanDuration.substring(1).trim());
                        predicates.add(cb.greaterThanOrEqualTo(root.get("durationDays"), val));
                        log.debug("Filter applied: durationDays >= {}", val);
                    } else if (cleanDuration.startsWith("-")) {
                        int val = Integer.parseInt(cleanDuration.substring(1).trim());
                        predicates.add(cb.lessThanOrEqualTo(root.get("durationDays"), val));
                        log.debug("Filter applied: durationDays <= {}", val);
                    } else {
                        int val = Integer.parseInt(cleanDuration);
                        predicates.add(cb.equal(root.get("durationDays"), val));
                        log.debug("Filter applied: durationDays == {}", val);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid duration format received: '{}'", durationStr);
                }
            }

            if (minRating != null && minRating.compareTo(BigDecimal.ZERO) >= 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
                log.debug("Filter added: rating >= {}", minRating);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
