package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.guide.GuideCreateDTO;
import com.desertakal.desertakal.model.dto.guide.GuideDTO;
import com.desertakal.desertakal.model.dto.guide.GuideFindDTO;
import com.desertakal.desertakal.model.dto.guide.GuideUpdateDTO;
import com.desertakal.desertakal.model.entity.Guide;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LanguageMapper.class, UserMapper.class},
        builder = @Builder(disableBuilder = true)
)
public abstract class GuideMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Named("toDto")
    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "photo", source = "photo", qualifiedByName = "toPhotoUrl")
    public abstract GuideDTO toDto(Guide guide);

    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "oauthProviders", expression = "java(guide.getOAuths() != null ? " +
            "guide.getOAuths().stream().map(auth -> auth.getProvider().name()).toList() : null)")
    @Mapping(target = "photo", source = "photo", qualifiedByName = "toPhotoUrl")
    public abstract GuideFindDTO toFindDto(Guide guide);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    public abstract Guide toEntity(GuideCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    public abstract void updateEntityFromDto(GuideUpdateDTO dto, @MappingTarget Guide guide);

    @IterableMapping(qualifiedByName = "toDto")
    public abstract List<GuideDTO> toDtos(List<Guide> guides);
}