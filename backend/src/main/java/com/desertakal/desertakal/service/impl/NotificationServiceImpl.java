package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.BusinessRuleException;
import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.notif.NotificationFindDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    @Transactional
    public NotificationFindDTO find(@NonNull UUID notifUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {
        log.info("Request to fetch and mark as seen Notification UUID: {}", notifUuid);

        Notification notification = repository.findByUuid(notifUuid)
                .orElseThrow(() -> {
                    log.error("Fetch failed: Notification with UUID {} not found", notifUuid);
                    return new ResourceNotFoundException("Notification", "identifier", notifUuid.toString());
                });

        boolean isOwner = notification.getUser().getUuid().equals(currentUserUuid);

        if (!isOwner && !isAdmin) {
            log.error("Unauthorized Access: User {} attempted to see notification {} owned by {}",
                    currentUserUuid, notifUuid, notification.getUser().getUuid());
            throw new UnauthorizedActionException("Access denied: You are not authorized to cancel this reservation.");
        }

        if (Boolean.FALSE.equals(notification.getSeen())) {
            log.debug("Updating status for notification {}: seen = true", notifUuid);
            notification.setSeen(true);
        } else {
            log.debug("Notification {} is already marked as seen", notifUuid);
        }

        log.info("Successfully retrieved and updated Notification: {}", notifUuid);

        return mapper.toFindDto(notification);
    }

    @Override
    public PaginationDTO findByUser(@NonNull UUID userUuid, @NonNull Pageable pageable) {
        log.info("Fetching notifications for user: {} [Page: {}, Size: {}]",
                userUuid, pageable.getPageNumber(), pageable.getPageSize());

        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.error("Failed to fetch notifications: User {} not found", userUuid);
                    return new ResourceNotFoundException("User", "identifier", userUuid.toString());
                });

        Specification<@NonNull Notification> spec = (root, query, cb) -> {
            log.debug("Building query specification for user: {}", user.getEmail());
            return cb.equal(root.get("user"), user);
        };

        var notifPage = repository.findAll(spec, pageable);

        log.info("Successfully retrieved {} notifications for user: {} (Total elements: {})",
                notifPage.getNumberOfElements(), userUuid, notifPage.getTotalElements());

        return PaginationDTO.builder()
                .content(mapper.toDtos(notifPage.getContent()))
                .page(notifPage.getNumber())
                .size(notifPage.getSize())
                .totalElements(notifPage.getTotalElements())
                .totalPages(notifPage.getTotalPages())
                .isFirst(notifPage.isFirst())
                .isLast(notifPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID notifUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {
        log.info("Request to delete Notification with UUID: {}", notifUuid);

        Notification notification = repository.findByUuid(notifUuid)
                .orElseThrow(() -> {
                    log.warn("Delete failed: Notification not found for UUID: {}", notifUuid);
                    return new ResourceNotFoundException("Notification", "identifier", notifUuid.toString());
                });

        boolean isOwner = notification.getUser().getUuid().equals(currentUserUuid);

        if (!isOwner && !isAdmin) {
            log.error("Unauthorized Access: User {} attempted to delete notification {} owned by {}",
                    currentUserUuid, notifUuid, notification.getUser().getUuid());
            throw new UnauthorizedActionException("Access denied: You are not authorized to cancel this reservation.");
        }

        repository.delete(notification);

        log.info("Successfully deleted Notification UUID: {}", notifUuid);
    }
}
