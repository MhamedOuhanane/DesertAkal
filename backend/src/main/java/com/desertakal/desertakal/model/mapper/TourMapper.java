package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.tour.TourCreateDTO;
import com.desertakal.desertakal.model.dto.tour.TourDTO;
import com.desertakal.desertakal.model.dto.tour.TourFindDTO;
import com.desertakal.desertakal.model.dto.tour.TourUpdateDTO;
import com.desertakal.desertakal.model.entity.Tour;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CityTourMapper.class},
        builder = @Builder(disableBuilder = true)
)
public abstract class TourMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Named("toDto")
    @Mapping(target = "image", source = "image", qualifiedByName = "toPublicUrl")
    public abstract TourDTO toDto(Tour tour);

    @Mapping(target = "image", source = "image", qualifiedByName = "toPublicUrl")
    public abstract TourFindDTO toFindDto(Tour tour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cityTours", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    public abstract Tour toEntity(TourCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    public abstract void updateEntityFromDto(TourUpdateDTO dto, @MappingTarget Tour tour);

    @IterableMapping(qualifiedByName = "toDto")
    public abstract List<TourDTO> toDtos(List<Tour> tours);

    @Named("toPublicUrl")
    protected String toPublicUrl(String imagePath) {
        return fileStorageService.getPublicUrl(imagePath);
    }
}