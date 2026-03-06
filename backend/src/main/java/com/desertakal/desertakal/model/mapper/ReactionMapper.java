package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.reaction.ReactionCreateDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionDTO;
import com.desertakal.desertakal.model.entity.Reaction;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ReactionMapper {
    @Autowired
    private FileStorageService fileStorageService;

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(source = "article.uuid", target = "articleUuid")
    @Mapping(source = "reaction.desc", target = "emoji")
    @Mapping(expression = "java(reaction.getUser().getFullName())", target = "userName")
    @Mapping(source = "user.photo", target = "userPhoto")
    public abstract ReactionDTO toDto(Reaction reaction);

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "uuid" , ignore = true)
    @Mapping(target = "reaction" , ignore = true)
    @Mapping(target = "createdAt" , ignore = true)
    @Mapping(target = "user" , ignore = true)
    @Mapping(target = "article" , ignore = true)
    public abstract Reaction toEntity(ReactionCreateDTO dto);

    public abstract List<ReactionDTO> toDtos(List<Reaction> reactions);

    @Named("toPhotoUrl")
    private String toPhotoUrl(String photo) {
        return fileStorageService.getPublicUrl(photo, FileType.PROFILE);
    }

}
