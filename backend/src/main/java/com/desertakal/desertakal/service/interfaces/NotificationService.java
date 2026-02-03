package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.notif.NotificationDTO;
import com.desertakal.desertakal.model.dto.notif.NotificationFindDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void create(String title, String message, UUID userUuid);
    NotificationFindDTO find(@NonNull UUID notifUuid);
    List<NotificationDTO> findByUser(@NonNull UUID userUuid, @NonNull Pageable pageable);
}
