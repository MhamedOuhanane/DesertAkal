package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.refreshToken.ActiveSessionDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenFullDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshTokenDTO create(@NonNull RefreshTokenRequestDTO dto);
    LoginDTO refresh(@NonNull String token, @NonNull RefreshTokenRequestDTO dto);
    List<ActiveSessionDTO> getActiveSessions(@NonNull UUID userUuid);
    RefreshTokenFullDTO find(@NonNull String token);

}
