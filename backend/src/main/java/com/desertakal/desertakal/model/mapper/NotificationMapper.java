package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.notif.NotificationDTO;
import com.desertakal.desertakal.model.dto.notif.NotificationFindDTO;
import com.desertakal.desertakal.model.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {


    NotificationDTO toDto(Notification notification);

    @Mapping(target = "userUuid", source = "user.uuid")
    @Mapping(target = "userName", expression = "java(notification.getUser().getFullName())")
    NotificationFindDTO toFindDto(Notification notification);

    List<NotificationDTO> toDtos(List<Notification> notifications);
}
