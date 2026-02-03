package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.notif.NotificationDTO;
import com.desertakal.desertakal.model.dto.notif.NotificationFindDTO;
import com.desertakal.desertakal.model.entity.Notification;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.mapper.NotificationMapper;
import com.desertakal.desertakal.repository.NotificationRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final UserRepository userRepository;


    @Override
    public void create(String title, String message, UUID userUuid) {
        log.info("Starting notification creation for User UUID: {} | Title: '{}'", userUuid, title);

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.error("Notification creation failed: User not found with UUID: {}", userUuid);
                    return new ResourceNotFoundException("User", "identifier", userUuid.toString());
                });

        log.debug("User found: {}. Mapping notification data...", user.getEmail());

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .date(LocalDateTime.now())
                .seen(false)
                .build();

        repository.save(notification);
        log.info("Notification successfully saved for User: {} [Notif UUID: {}]",
                userUuid, notification.getUuid());
    }

    @Override
    public NotificationFindDTO find(@NonNull UUID notifUuid) {
        return null;
    }

    @Override
    public List<NotificationDTO> findByUser(@NonNull UUID userUuid, @NonNull Pageable pageable) {
        return List.of();
    }
}
