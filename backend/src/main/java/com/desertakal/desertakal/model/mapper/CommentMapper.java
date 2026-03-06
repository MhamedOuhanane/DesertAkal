package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.comment.CommentCreateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.entity.Comment;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CommentMapper {
    @Autowired
    private FileStorageService fileStorageService;

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(source = "article.uuid", target = "articleUuid")
    @Mapping(expression = "java(comment.getUser().getFullName())", target = "userName")
    @Mapping(source = "user.photo", target = "userPhoto", qualifiedByName = "toPhotoUrl")
    public abstract CommentDTO toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    public abstract Comment toEntity(CommentCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    public abstract void updateEntityFromDto(CommentCreateDTO dto, @MappingTarget Comment comment);

    public abstract List<CommentDTO> toDtos(List<Comment> comments);

    @Named("toPhotoUrl")
    protected String toPhotoUrl(String photo) {
        return fileStorageService.getPublicUrl(photo, FileType.PROFILE);
    }
}
