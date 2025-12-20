package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.reaction.ReactionCreateDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionDTO;
import com.desertakal.desertakal.model.entity.Reaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(source = "article.uuid", target = "articleUuid")
    @Mapping(source = "reaction.getDesc()", target = "emoji")
    @Mapping(expression = "java(reservation.getUser().getFirstName() + \" \" + reservation.getUser().getLastName())", target = "userName")
    @Mapping(source = "user.photo", target = "userPhoto")
    ReactionDTO toDto(Reaction article);

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "uuid" , ignore = true)
    @Mapping(target = "reaction" , ignore = true)
    @Mapping(target = "createdAt" , ignore = true)
    @Mapping(target = "user" , ignore = true)
    @Mapping(target = "article" , ignore = true)
    Reaction toEntity(ReactionCreateDTO dto);

    List<ReactionDTO> toDtos(List<Reaction> articles);
}
