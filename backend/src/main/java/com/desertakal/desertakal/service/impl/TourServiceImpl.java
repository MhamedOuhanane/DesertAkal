package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import com.desertakal.desertakal.model.mapper.TourMapper;
import com.desertakal.desertakal.repository.TourRepository;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.desertakal.desertakal.service.interfaces.TourService;
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
public class TourServiceImpl implements TourService {
    private final TourRepository repository;
    private final TourMapper mapper;
    private final FileStorageService fileStorageService;

    @Override
    public TourFindDTO create(@NonNull TourCreateDTO dto, @NonNull MultipartFile image) {
        return null;
    }

    @Override
    public TourFindDTO update(@NonNull UUID tourUuid, @NonNull TourUpdateDTO dto) {
        return null;
    }

    @Override
    public TourFindDTO find(@NonNull UUID tourUuid) {
        return null;
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
}
