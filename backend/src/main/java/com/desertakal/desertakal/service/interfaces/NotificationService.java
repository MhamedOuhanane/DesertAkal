package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.notif.NotificationFindDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    void create(String title, String message, UUID userUuid);
    NotificationFindDTO find(@NonNull UUID notifUuid, @NonNull UUID currentUserUuid, boolean isAdmin);
    PaginationDTO findByUser(@NonNull UUID userUuid, @NonNull Pageable pageable);
    void delete(@NonNull UUID notifUuid, @NonNull UUID currentUserUuid, boolean isAdmin);
}
