package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TouristService {
    TouristDTO find(UUID touristUuid);
    TouristDTO updateAvatar(@NonNull UUID touristUuid, @NonNull MultipartFile avatar);
    UserFindDTO update(@NonNull UUID touristUuid, @NonNull TouristUpdateDTO dto);
}
