package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Notification;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<@NonNull Notification, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Notification> {
    Optional<@NonNull Notification> findByUuid(@NonNull UUID uuid);

}
