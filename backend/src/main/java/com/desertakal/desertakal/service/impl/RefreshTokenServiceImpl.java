package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.Security.jwt.JwtService;
import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.auth.LoginDTO;
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
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public RefreshTokenDTO create(@NonNull RefreshTokenRequestDTO dto) {
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

    @Transactional
    @Override
    public LoginDTO refresh(@NonNull String token, @NonNull RefreshTokenRequestDTO dto) {
        RefreshToken oldToken = repository.findByToken(token).orElseThrow(() ->
                    new ResourceNotFoundException("Refresh token", "token", token)
                );

        if (oldToken.isUsed() || oldToken.isReuseDetected()) {
            handleSecurityBreach(oldToken);
            throw new BadRequestException("Security Alert: This token has already been used. All related sessions revoked.");
        }

        if (oldToken.isRevoked())
            throw new BadRequestException("This session has been revoked. Please login again.");

        if (oldToken.getDeviceId() != null && !oldToken.getDeviceId().equals(dto.getDeviceId())) {
            handleSecurityBreach(oldToken);
            throw new BadRequestException("Security Alert: Device mismatch detected.");
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Refresh token expired.");

        oldToken.setUsed(true);
        oldToken.setUsedAt(LocalDateTime.now());

        User user = oldToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        RefreshToken newToken = RefreshToken.builder()
                .uuid(UUID.randomUUID())
                .token(newRefreshToken)
                .user(user)
                .familyId(oldToken.getFamilyId())
                .parentToken(oldToken.getToken())
                .deviceId(oldToken.getDeviceId())
                .ipAddress(oldToken.getIpAddress())
                .userAgent(oldToken.getUserAgent())
                .expiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        repository.save(newToken);

        return LoginDTO.builder()
                .uuid(user.getUuid())
                .username(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .refreshToken(newRefreshToken)
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    public RefreshTokenFullDTO find(@NonNull String token) {
        return null;
    }

    @Override
    public List<RefreshTokenDTO> findAll(@NonNull Map<String, Object> map) {
        return List.of();
    }

    @Override
    public List<RefreshTokenDTO> findAllByUser(@NonNull UUID userUuid, @NonNull Map<String, Object> map) {
        return List.of();
    }

    private void handleSecurityBreach(RefreshToken compromisedToken) {
        compromisedToken.setReuseDetected(true);
        compromisedToken.setRevoked(true);
        compromisedToken.setRevokedAt(LocalDateTime.now());

    }
}
