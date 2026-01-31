package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenFullDTO;
import com.desertakal.desertakal.model.dto.refreshToken.RefreshTokenRequestDTO;
import com.desertakal.desertakal.model.entity.RefreshToken;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenMapper {

    @Named("toDto")
    @Mapping(source = "user.uuid", target = "userUuid")
    RefreshTokenDTO toDto(RefreshToken refreshToken);

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(expression = "java(refreshToken.getUser() != null ? " +
            "refreshToken.getUser().getFullName() : null)",
            target = "userName")
    RefreshTokenFullDTO toFindDto(RefreshToken refreshToken);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "token", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "familyId", ignore = true)
    @Mapping(target = "parentToken", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "usedAt", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    @Mapping(target = "revoked", ignore = true)
    @Mapping(target = "used", ignore = true)
    @Mapping(target = "reuseDetected", ignore = true)
    @Mapping(target = "version", ignore = true)
    RefreshToken toEntity(RefreshTokenRequestDTO dto);

    @IterableMapping(qualifiedByName = "toDto")
    List<RefreshTokenDTO> toDtos(List<RefreshToken> refreshTokens);
}
