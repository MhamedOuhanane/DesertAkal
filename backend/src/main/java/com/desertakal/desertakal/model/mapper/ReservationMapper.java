package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.entity.Reservation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "tour.uuid", target = "tourUuid")
    @Mapping(source = "tour.title", target = "tourTitle")
    @Mapping(source = "guide.uuid", target = "guideUuid")
    @Mapping(expression = "java(reservation.getGuide() != null ? reservation.getGuide().getFirstName() + \" \" + reservation.getGuide().getLastName() : null)", target = "guideName")
    @Mapping(source = "tourist.uuid", target = "touristUuid")
    @Mapping(expression = "java(reservation.getTourist().getFirstName() + \" \" + reservation.getTourist().getLastName())", target = "touristName")
    @Mapping(source = "tourist.photo", target = "touristPhoto")
    ReservationDTO toDto(Reservation reservation);

    @InheritConfiguration(name = "toDto")
    ReservationFindDTO toFindDto(Reservation reservation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "qrCode", ignore = true)
    @Mapping(target = "pdfUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "guide", ignore = true)
    @Mapping(target = "tourist", ignore = true)
    @Mapping(target = "payments", ignore = true)
    Reservation toEntity(ReservationCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(ReservationUpdateDTO dto, @MappingTarget Reservation reservation);

    List<ReservationDTO> toDtos(List<Reservation> reservations);
}