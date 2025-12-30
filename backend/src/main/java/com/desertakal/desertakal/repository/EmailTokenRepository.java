package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.EmailVerificationToken;
import com.desertakal.desertakal.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailTokenRepository extends JpaRepository<@NonNull EmailVerificationToken, @NonNull UUID> {
    Optional<EmailVerificationToken> findByToken(String token);

    List<EmailVerificationToken> findByUser(User user);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    boolean existsByUserAndExpiryDateAfter(User user, LocalDateTime now);
}
