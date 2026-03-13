package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.city.CityCreateDTO;
import com.desertakal.desertakal.model.dto.city.CityDTO;
import com.desertakal.desertakal.model.dto.city.CityFindDTO;
import com.desertakal.desertakal.model.dto.city.CityUpdateDTO;
import com.desertakal.desertakal.model.entity.City;
import com.desertakal.desertakal.model.entity.Image;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ImageMapper.class})
public abstract class CityMapper {
    @Autowired
    protected FileStorageService fileStorageService;

    @Named("toDto")
    @Mapping(target = "coverImage", source = "images", qualifiedByName = "extractCoverImage")
    public abstract CityDTO toDto(City city);
    public abstract CityFindDTO toFindDto(City city);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "cityTours", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract City toEntity(CityCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDto(CityUpdateDTO dto, @MappingTarget City city);

    @IterableMapping(qualifiedByName = "toDto")
    public abstract List<CityDTO> toDtos(List<City> cities);

    @Named("extractCoverImage")
    protected String extractCoverImage(List<Image> images) {
        String path = "";
        if (images != null && !images.isEmpty()) {
            path = images.stream()
                    .filter(Image::getIsCover)
                    .findFirst()
                    .map(Image::getImage)
                    .orElse("");
        }

        return fileStorageService.getPublicUrl(path, FileType.CITY);
    }
}
