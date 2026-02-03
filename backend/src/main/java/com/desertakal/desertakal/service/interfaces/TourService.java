package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TourService {
    TourFindDTO create(@NonNull TourCreateDTO dto, @NonNull MultipartFile image);
    TourFindDTO update(@NonNull UUID tourUuid, @NonNull TourUpdateDTO dto);
    TourFindDTO updateImage(@NonNull UUID tourUuid, @NonNull MultipartFile image);
    TourFindDTO find(@NonNull UUID tourUuid);
    PaginationDTO findAll(String search, String city, String durationStr, BigDecimal minRating, @NonNull Pageable pageable);
    void delete(@NonNull UUID tourUuid);
    List<TourDTO> findTop5();
    PaginationDTO findAllByTourist(@NonNull UUID touristUuid, @NonNull Pageable pageable);
    PaginationDTO findAllByGuide(@NonNull UUID guideUuid, @NonNull Pageable pageable);
}
