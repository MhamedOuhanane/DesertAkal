package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.review.ReviewCreateDTO;
import com.desertakal.desertakal.model.dto.review.ReviewDTO;
import com.desertakal.desertakal.model.dto.review.ReviewUpdateDTO;
import com.desertakal.desertakal.model.entity.Review;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ReviewMapper {

    @Autowired
    private FileStorageService fileStorageService;

    @Named("toDto")
    @Mapping(source = "tourist.uuid", target = "touristUuid")
    @Mapping(expression = "java(review.getTourist().getFullName())", target = "touristName")
    @Mapping(source = "tourist.photo", target = "touristPhoto", qualifiedByName = "toPhotoUrl")
    @Mapping(target = "reviewableName", expression = "java(review.Re)")
    public abstract ReviewDTO toDto(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tourist", ignore = true)
    public abstract Review toEntity(ReviewCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "reviewableUuid", ignore = true)
    @Mapping(target = "reviewableType", ignore = true)
    public abstract void updateEntityFromDto(ReviewUpdateDTO dto, @MappingTarget Review review);

    @IterableMapping(qualifiedByName = "toDto")
    public abstract List<ReviewDTO> toDtos(List<Review> reviews);

    @Named("toPhotoUrl")
    protected String toPhotoUrl(String photo) {
        return fileStorageService.getPublicUrl(photo, FileType.PROFILE);
    }
}
