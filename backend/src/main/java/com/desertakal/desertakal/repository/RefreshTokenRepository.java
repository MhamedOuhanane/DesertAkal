package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.RefreshToken;
import com.desertakal.desertakal.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshToken, @NonNull UUID> {
    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUuid(UUID uuid);

    List<RefreshToken> findByUserAndDeviceId(User user, String deviceId);

    List<RefreshToken> findAllByFamilyId(UUID familyId);

    Optional<RefreshToken> findByTokenAndRevoked(String token, boolean Revoked);
    Optional<RefreshToken> findByTokenAndUsed(String token, boolean Used);

    List<RefreshToken> findAllByUserAndRevoked(User user, boolean Revoked);
    List<RefreshToken> findAllByUserAndUsed(User user, boolean Used);

    @EntityGraph(attributePaths = {"user"})
    List<RefreshToken> findAllByUserAndRevokedFalseAndUsedFalseOrderByCreatedAtDesc(User user);
    List<RefreshToken> findAllByUserAndReuseDetectedFalse(User user);

    @Transactional
    void deleteByUser(User user);
    @Transactional
    void deleteByFamilyId(UUID familyId);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

}
