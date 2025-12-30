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

    List<RefreshToken> findByUserAndDeviceId(User user, String deviceId);

    List<RefreshToken> findAllByFamilyId(String familyId);

    Optional<RefreshToken> findAllByTokenAndIsRevoked(String token, boolean isRevoked);
    Optional<RefreshToken> findAllByTokenAndIsUsed(String token, boolean isUsed);

    List<RefreshToken> findAllByUserAndIsRevoked(User user, boolean isRevoked);
    List<RefreshToken> findAllByUserAndIsUsed(User user, boolean isUsed);

    @EntityGraph(attributePaths = {"user"})
    List<RefreshToken> findAllByUserAndRevokedFalseAndUsedFalseByCreatedAtDesc(User user);
    List<RefreshToken> findAllByUserAndIsReuseDetectedFalse(User user);

    @Transactional
    void deleteByUser(User user);
    @Transactional
    void deleteByFamilyId(String familyId);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

}
