package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.Security.jwt.JwtService;
import com.desertakal.desertakal.Security.user.CustomUserDetails;
import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenFullDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;
import com.desertakal.desertakal.model.entity.RefreshToken;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.mapper.RefreshTokenMapper;
import com.desertakal.desertakal.repository.RefreshTokenRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.RefreshTokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final RefreshTokenMapper mapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    @Override
    public RefreshTokenDTO create(RefreshTokenRequestDTO dto) {
        if (dto.getUserUuid() == null)
            throw new BadRequestException("The user's UUID is missing from the request.");

        User user = userRepository.findByUuid(dto.getUserUuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "identifier", dto.getUserUuid().toString())
                );

        String token = jwtService.generateRefreshToken(user);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        RefreshToken refreshToken = mapper.toEntity(dto);

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setFamilyId(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(
                expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        repository.save(refreshToken);

        return mapper.toDto(refreshToken);
    }

    @Override
    public RefreshTokenDTO refresh(String token, RefreshTokenRequestDTO dto) {
        return null;
    }

    @Override
    public RefreshTokenFullDTO find(String token) {
        return null;
    }

    @Override
    public List<RefreshTokenDTO> findAll(Map<String, Object> map) {
        return List.of();
    }

    @Override
    public List<RefreshTokenDTO> findAllByUser(UUID userUuid, Map<String, Object> map) {
        return List.of();
    }
}
