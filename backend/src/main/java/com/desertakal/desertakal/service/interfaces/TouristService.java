package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.enums.UserStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

public interface TouristService {
    PaginationDTO findAll(String search, UserStatus status, String nationality, @NonNull Pageable pageable);
}
