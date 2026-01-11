package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.auth.LoginRequestDTO;
import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import org.jspecify.annotations.NonNull;

public interface UserService {
    void register(@NonNull RegisterDTO dto);
    LoginDTO login(@NonNull LoginRequestDTO dto, @NonNull String ipAddress, @NonNull String userAgent);
}
