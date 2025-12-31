package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.auth.RegisterDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristDTO;
import com.desertakal.desertakal.model.dto.tourist.TouristUpdateDTO;
import com.desertakal.desertakal.model.entity.Tourist;
import com.desertakal.desertakal.model.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TouristMapper {

    TouristDTO toDto(Tourist tourist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "photo")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    Tourist toEntity(RegisterDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntityFromDto(TouristUpdateDTO dto, @MappingTarget Tourist tourist);

    List<TouristDTO> toDtos(List<Tourist> tourists);
}
