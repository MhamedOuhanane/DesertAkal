package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.comment.CommentCreateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.entity.Comment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(source = "article.uuid", target = "articleUuid")
    @Mapping(expression = "java(comment.getUser().getFullName())", target = "userName")
    @Mapping(source = "user.photo", target = "userPhoto")
    CommentDTO toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    Comment toEntity(CommentCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(CommentCreateDTO dto, @MappingTarget Comment comment);

    List<CommentDTO> toDtos(List<Comment> comments);
}
