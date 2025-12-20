package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.user.UserDTO;
import com.desertakal.desertakal.model.dto.user.UserFindDTO;
import com.desertakal.desertakal.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role", target = "role.name")
    UserDTO toDto(User user);

    @Mapping(target = "oauthProviders", expression = "java(user.getOAuths() != null ? " +
            "user.getOAuths().stream().map(auth -> auth.getProvider().name()).toList() : null)")
    UserFindDTO toFindDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(RegisterDTO dto);

    List<UserDTO> toDtos(List<User> users);
    List<UserFindDTO> toFindDtos(List<User> users);
}
