package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityDTO;
import com.desertakal.desertakal.model.dto.city.CityFindDTO;
import com.desertakal.desertakal.model.dto.city.CityUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CityService {
    CityFindDTO create(@NonNull CityCreateDTO dto);
    CityFindDTO find(@NonNull UUID cityUuid);
    PaginationDTO findAll(String search, @NonNull Pageable pageable);
    List<CityDTO> findByTour(@NonNull UUID tourUuid);
    CityFindDTO update(@NonNull UUID cityUuid, @NonNull CityUpdateDTO dto);
    void delete(@NonNull UUID cityUuid);
    CityFindDTO addImages(@NonNull UUID cityUuid, @NonNull List<MultipartFile> images);
    void deleteImage(@NonNull UUID cityUuid, @NonNull List<UUID> imageUuids);
    void setCoverImage(@NonNull UUID cityUuid, @NonNull UUID imageUuid);
}
