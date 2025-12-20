package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.notif.NotificationCreateDTO;
import com.desertakal.desertakal.model.dto.notif.NotificationDTO;
import com.desertakal.desertakal.model.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userUuid", source = "user.uuid")
    @Mapping(target = "userName", expression = "java(notification.getUser().getFirstName() + \" \" + notification.getUser().getLastName())")
    NotificationDTO toDto(Notification notification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "seen", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "user", ignore = true)
    Notification toEntity(NotificationCreateDTO dto);

    List<NotificationDTO> toDtos(List<Notification> notifications);
}
