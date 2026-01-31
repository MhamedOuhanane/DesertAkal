package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.enums.FileType;
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
public abstract class TouristMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "oauthProviders", expression = "java(tourist.getOAuths() != null ? " +
            "tourist.getOAuths().stream().map(auth -> auth.getProvider().name()).toList() : null)")
    @Mapping(target = "photo", source = "photo", qualifiedByName = "toPhotoUrl")
    @Mapping(target = "avatarUrl", source = "avatarUrl", qualifiedByName = "toAvatarUrl")
    public abstract TouristDTO toDto(Tourist tourist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    public abstract Tourist toEntity(RegisterDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "OAuths", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    public abstract void updateEntityFromDto(TouristUpdateDTO dto, @MappingTarget Tourist tourist);

    public abstract List<TouristDTO> toDtos(List<Tourist> tourists);

    @Named("toAvatarUrl")
    protected String toAvatarUrl(String avatarUrl) {
        return fileStorageService.getPublicUrl(avatarUrl, FileType.AVATAR);
    }
}