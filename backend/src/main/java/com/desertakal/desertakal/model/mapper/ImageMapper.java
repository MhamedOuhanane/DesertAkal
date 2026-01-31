package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.image.ImageDTO;
import com.desertakal.desertakal.model.entity.Image;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ImageMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Mapping(target = "cityUuid", source = "city.uuid")
    @Mapping(target = "image", source = "image", qualifiedByName = "toPublicUrl")
    public abstract ImageDTO toDto(Image image);

    public abstract List<ImageDTO> toDtos(List<Image> images);

    @Named("toPublicUrl")
    protected String toPublicUrl(String imagePath) {
        return fileStorageService.getPublicUrl(imagePath, FileType.CITY);
    }
}