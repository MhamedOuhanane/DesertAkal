package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenFullDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshTokenDTO create(RefreshTokenRequestDTO dto);
    RefreshTokenDTO refresh(String token, RefreshTokenRequestDTO dto);
    RefreshTokenFullDTO find(String token);
    List<RefreshTokenDTO> findAll(Map<String, Object> map);
    List<RefreshTokenDTO> findAllByUser(UUID userUuid, Map<String, Object> map);
}
