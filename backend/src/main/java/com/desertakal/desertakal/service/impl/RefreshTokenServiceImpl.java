package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.Security.jwt.JwtService;
import com.desertakal.desertakal.exception.custom.BadRequestException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.auth.LoginDTO;
import com.desertakal.desertakal.model.dto.refreshToken.*;
import com.desertakal.desertakal.model.entity.RefreshToken;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.mapper.RefreshTokenMapper;
import com.desertakal.desertakal.repository.RefreshTokenRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.RefreshTokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final RefreshTokenMapper mapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public RefreshTokenDTO create(@NonNull RefreshTokenRequestDTO dto) {
        log.info("Creating new Refresh Token session for User UUID: {} on Device: {}",
                dto.getUserUuid(), dto.getDeviceId());

        if (dto.getUserUuid() == null) {
            log.error("Failed to create Refresh Token: User UUID is missing.");
            throw new BadRequestException("The user's UUID is missing from the request.");
        }

        User user = userRepository.findByUuid(dto.getUserUuid())
                .orElseThrow(() -> {
                    log.warn("User not found for UUID: {}", dto.getUserUuid());
                    return new ResourceNotFoundException("User", "identifier", dto.getUserUuid().toString());
                });

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

        log.info("Successfully created Refresh Token session. User: {}, FamilyId: {}, IP: {}, Expires at: {}",
                user.getEmail(), refreshToken.getFamilyId(), dto.getIpAddress(), refreshToken.getExpiresAt());

        return mapper.toDto(refreshToken);
    }

    @Transactional
    @Override
    public LoginDTO refresh(@NonNull String token, @NonNull RefreshTokenRequestDTO dto) {
        log.info("Attempting to refresh token for device: {} from IP: {}", dto.getDeviceId(), dto.getIpAddress());

        RefreshToken oldToken = repository.findByToken(token).orElseThrow(() -> {
            log.warn("Refresh attempt failed: Token not found in database.");
            return new ResourceNotFoundException("Refresh token", "token", token);
        });

        if (oldToken.isUsed() || oldToken.isReuseDetected()) {
            log.error("SECURITY ALERT: Reuse detected for User: {}. FamilyId: {}. ParentToken: {}",
                    oldToken.getUser().getEmail(), oldToken.getFamilyId(), oldToken.getParentToken());
            handleSecurityBreach(oldToken);
            throw new BadRequestException("Security Alert: This token has already been used. All related sessions revoked.");
        }

        if (oldToken.isRevoked()){
            log.warn("Revoked token access attempt: FamilyId {}, User {}",
                    oldToken.getFamilyId(), oldToken.getUser().getEmail());
            throw new BadRequestException("This session has been revoked. Please login again.");
        }

        if (oldToken.getDeviceId() != null && !oldToken.getDeviceId().equals(dto.getDeviceId())) {
            log.warn("SECURITY WARNING: Device mismatch. Expected {}, but got {}. User: {}",
                    oldToken.getDeviceId(), dto.getDeviceId(), oldToken.getUser().getEmail());
            handleSecurityBreach(oldToken);
            throw new BadRequestException("Security Alert: Device mismatch detected.");
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Token expired for user: {}. Expiration date: {}", oldToken.getUser().getEmail(), oldToken.getExpiresAt());
            throw new BadRequestException("Refresh token expired.");
        }

        oldToken.setUsed(true);
        oldToken.setUsedAt(LocalDateTime.now());

        User user = oldToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        Date expiration = jwtService.extractClaim(newRefreshToken, Claims::getExpiration);

        RefreshToken newToken = RefreshToken.builder()
                .uuid(UUID.randomUUID())
                .token(newRefreshToken)
                .user(user)
                .familyId(oldToken.getFamilyId())
                .parentToken(oldToken.getToken())
                .deviceId(oldToken.getDeviceId())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .expiresAt(expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();

        repository.save(newToken);

        log.info("Token successfully rotated for user: {}. New Token UUID: {}", user.getEmail(), newToken.getUuid());

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
    public List<ActiveSessionDTO> getActiveSessions(@NonNull UUID userUuid) {
        log.info("Request received to fetch active sessions for User UUID: {}", userUuid);

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("User not found for UUID: {}", userUuid);
                    return new ResourceNotFoundException("User", "identifier", userUuid.toString());
                });

        log.info("Fetching active sessions for user: {}", user.getEmail());

        List<RefreshToken> activeTokens = repository.findAllByUserAndRevokedFalseAndUsedFalseByCreatedAtDesc(user);

        Map<String, RefreshToken> uniqueSessions = activeTokens.stream()
                .collect(Collectors.toMap(
                        RefreshToken::getDeviceId,
                        token -> token,
                        (existing, replacement) -> existing
                ));

        log.info("Found {} active sessions for user: {}", activeTokens.size(), user.getEmail());

        return uniqueSessions.values().stream()
                .map(token -> ActiveSessionDTO.builder()
                        .sessionUuid(token.getUuid())
                        .ipAddress(token.getIpAddress())
                        .userAgent(token.getUserAgent())
                        .lastActive(token.getCreatedAt())
                        .expiresAt(token.getExpiresAt())
                        .build()
                )
                .sorted(Comparator.comparing(ActiveSessionDTO::getLastActive).reversed())
                .toList();
    }

    @Override
    public RefreshTokenFullDTO find(@NonNull String token) {
        RefreshToken refreshToken = repository.findByToken(token).orElseThrow(() -> {
            log.warn("Refresh attempt failed: Token not found in database.");
            return new ResourceNotFoundException("Refresh token", "token", token);
        });

        return mapper.toFindDto(refreshToken);
    }

    @Transactional
    @Override
    public void remoteLogout(@NonNull UUID userUuid, @NonNull RemoteLogoutRequestDTO dto) {
        log.info("Remote logout initiated by user uuid: {} for session UUID: {}",
                userUuid, dto.getSessionUuid());

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("User not found for UUID: {}", userUuid);
                    return new ResourceNotFoundException("User", "identifier", userUuid.toString());
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Security alert: Incorrect password during remote logout attempt by user: {}",
                    user.getEmail());
            throw new BadRequestException("Invalid password. Action denied.");
        }

        RefreshToken targetToken = repository.findByUuid(dto.getSessionUuid())
                .orElseThrow(() -> {
                    log.warn("Logout failed: Session UUID {} not found", dto.getSessionUuid().toString());
                    return new ResourceNotFoundException("Session", "id", dto.getSessionUuid().toString());
                });

        String familyId = targetToken.getFamilyId();
        repository.deleteByFamilyId(familyId);

        log.info("Successfully revoked all tokens in family: {} for user: {}",
                familyId, user.getEmail());
    }

    private void handleSecurityBreach(RefreshToken compromisedToken) {
        log.error("BREACH HANDLER: Revoking entire family {} for user {}",
                compromisedToken.getFamilyId(), compromisedToken.getUser().getEmail());

        compromisedToken.setReuseDetected(true);
        compromisedToken.setRevoked(true);
        compromisedToken.setRevokedAt(LocalDateTime.now());

        repository.deleteByFamilyId(compromisedToken.getFamilyId());

        log.info("Family {} has been purged from the system.", compromisedToken.getFamilyId());
    }
}
