package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.auth.LoginRequestDTO;
import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.dto.user.UserDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import com.desertakal.desertakal.model.enums.UserStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface UserService {
    void register(@NonNull RegisterDTO dto);
    LoginDTO login(@NonNull LoginRequestDTO dto, @NonNull String ipAddress, @NonNull String userAgent);
    PaginationDTO findAll(@NonNull Pageable pageable);
    UserFindDTO find(@NonNull UUID userUuid);
    UserFindDTO update(@NonNull UUID userUuid, @NonNull UserUpdateDTO dto);
//    UserFindDTO updateStatus(@NonNull UUID userUuid, @NonNull UserStatus status);
//    UserFindDTO updatePhoto(@NonNull UUID userUuid, @NonNull MultipartFile photo);
//    void delete(@NonNull UUID userUuid);
}
