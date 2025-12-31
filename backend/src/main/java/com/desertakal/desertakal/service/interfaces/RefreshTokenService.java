package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.refreshToken.*;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshTokenDTO create(@NonNull RefreshTokenRequestDTO dto);
    LoginDTO refresh(@NonNull String token, @NonNull RefreshTokenRequestDTO dto);
    List<ActiveSessionDTO> getActiveSessions(@NonNull UUID userUuid);
    RefreshTokenFullDTO find(@NonNull String token);
    void remoteLogout(@NonNull UUID userUuid, @NonNull RemoteLogoutRequestDTO dto);

}
