package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.comment.CommentCreateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.entity.Comment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(source = "article.uuid", target = "articleUuid")
    @Mapping(expression = "java(reservation.getUser().getFirstName() + \" \" + reservation.getUser().getLastName())", target = "userName")
    @Mapping(source = "user.photo", target = "userPhoto")
    CommentDTO toDto(Comment article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    Comment toEntity(CommentCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(CommentCreateDTO dto, @MappingTarget Comment article);

    List<CommentDTO> toDtos(List<Comment> articles);
}
