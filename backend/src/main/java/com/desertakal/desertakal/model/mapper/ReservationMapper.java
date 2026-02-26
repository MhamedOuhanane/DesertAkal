package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.reservation.ReservationCreateDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.dto.reservation.ReservationUpdateDTO;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PaymentMapper.class, FileStorageService.class}
)
public abstract class ReservationMapper {

    @Autowired
    protected FileStorageService fileStorageService;


    @Named("toDto")
    @Mapping(source = "tour.uuid", target = "tourUuid")
    @Mapping(source = "tour.title", target = "tourTitle")
    @Mapping(source = "guide.uuid", target = "guideUuid")
    @Mapping(expression = "java(reservation.getGuide() != null ? reservation.getGuide().getFullName() : null)", target = "guideName")
    @Mapping(source = "tourist.uuid", target = "touristUuid")
    @Mapping(expression = "java(reservation.getTourist().getFullName())", target = "touristName")
    @Mapping(source = "tourist.photo", target = "touristPhoto", qualifiedByName = "toPhotoUrl")
    @Mapping(source = "guide.photo", target = "guidePhoto", qualifiedByName = "toPhotoUrl")
    @Mapping(source = "reference", target = "reference")
    public abstract ReservationDTO toDto(Reservation reservation);

    @Mapping(source = "tour.uuid", target = "tourUuid")
    @Mapping(source = "tour.title", target = "tourTitle")
    @Mapping(source = "guide.uuid", target = "guideUuid")
    @Mapping(expression = "java(reservation.getGuide() != null ? reservation.getGuide().getFullName() : null)", target = "guideName")
    @Mapping(source = "tourist.uuid", target = "touristUuid")
    @Mapping(expression = "java(reservation.getTourist().getFullName())", target = "touristName")
    @Mapping(source = "tourist.photo", target = "touristPhoto", qualifiedByName = "toPhotoUrl")
    @Mapping(source = "guide.photo", target = "guidePhoto", qualifiedByName = "toPhotoUrl")
    @Mapping(source = "qrCode", target = "qrCode", qualifiedByName = "toFile")
    @Mapping(source = "pdfUrl", target = "pdfUrl", qualifiedByName = "toFile")
    @Mapping(source = "payments", target = "payments")
    @Mapping(source = "reference", target = "reference")
    public abstract ReservationFindDTO toFindDto(Reservation reservation);

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
    public abstract Reservation toEntity(ReservationCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    public abstract void updateEntityFromDto(ReservationUpdateDTO dto, @MappingTarget Reservation reservation);

    @IterableMapping(qualifiedByName = "toDto")
    public abstract List<ReservationDTO> toDtos(List<Reservation> reservations);

    @Named("toPhotoUrl")
    protected String toPhotoUrl(String photo) {
        return fileStorageService.getPublicUrl(photo, FileType.PROFILE);
    }


    @Named("toFile")
    protected String toFile(String photo) {
        return fileStorageService.getPublicUrl(photo, FileType.PROFILE);
    }
}