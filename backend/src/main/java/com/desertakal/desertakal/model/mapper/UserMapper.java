package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.user.UserDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.dto.user.UserUpdateDTO;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Named("toDto")
    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "photo", source = "photo", qualifiedByName = "toPhotoUrl")
    public abstract UserDTO toDto(User user);

    @Mapping(target = "oauthProviders", expression = "java(user.getOAuths() != null ? " +
            "user.getOAuths().stream().map(auth -> auth.getProvider().name()).toList() : null)")
    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "photo", expression = "java(fileStorageService.getPublicUrl(user.getPhoto()))")
    public abstract UserFindDTO toFindDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    public abstract User toEntity(RegisterDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "OAuths", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "emailVerificationTokens", ignore = true)
    public abstract void updateEntityFromDto(UserUpdateDTO dto, @MappingTarget User user);

    @IterableMapping(qualifiedByName = "toDto")
    public abstract List<UserDTO> toDtos(List<User> users);

    @Named("toAvatarUrl")
    protected String toAvatarUrl(String avatarPath) {
        return fileStorageService.getPublicUrl(avatarPath);
    }
}