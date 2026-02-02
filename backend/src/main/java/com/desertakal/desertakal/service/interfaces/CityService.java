package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityDTO;
import com.desertakal.desertakal.model.dto.city.CityFIndDTO;
import com.desertakal.desertakal.model.dto.city.CityUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CityService {
    CityFIndDTO create(@NonNull CityCreateDTO dto);
    CityFIndDTO find(@NonNull UUID cityUuid);
    PaginationDTO findAll(String search, @NonNull Pageable pageable);
    List<CityDTO> findByTour(@NonNull UUID tourUuid);
    CityFIndDTO update(@NonNull UUID cityUuid, @NonNull CityUpdateDTO dto);
    void delete(@NonNull UUID cityUuid);
}
