package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.enums.UserStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TouristService {
    PaginationDTO findAll(String search, UserStatus status, String nationality, @NonNull Pageable pageable);
    TouristDTO updateAvatar(@NonNull UUID touristUuid, @NonNull MultipartFile avatar);
    UserFindDTO update(@NonNull UUID touristUuid, @NonNull TouristUpdateDTO dto);
}
