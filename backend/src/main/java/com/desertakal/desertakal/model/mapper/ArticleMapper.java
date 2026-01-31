package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.article.ArticleCreateDTO;
import com.desertakal.desertakal.model.dto.article.ArticleDTO;
import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.enums.FileType;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public abstract class ArticleMapper {

    @Autowired
    protected FileStorageService fileStorageService;

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(target = "userName", expression = "java(article.getUser().getFullName())")
    @Mapping(target = "userPhoto", source = "user.photo", qualifiedByName = "toPublicUrl")
    @Mapping(target = "coverImage", source = "coverImage", qualifiedByName = "toPublicUrl")
    public abstract ArticleDTO toDto(Article article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "coverImage", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    public abstract Article toEntity(ArticleCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    public abstract void updateEntityFromDto(ArticleCreateDTO dto, @MappingTarget Article article);

    public abstract List<ArticleDTO> toDtos(List<Article> articles);

    @Named("toPublicUrl")
    protected String toPublicUrl(String path) {
        return fileStorageService.getPublicUrl(path, FileType.ARTICLE);
    }
}