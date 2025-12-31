package com.desertakal.desertakal.model.mapper;

import com.desertakal.desertakal.model.dto.article.ArticleCreateDTO;
import com.desertakal.desertakal.model.dto.article.ArticleDTO;
import com.desertakal.desertakal.model.entity.Article;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(source = "user.uuid", target = "userUuid")
    @Mapping(expression = "java(article.getUser().getFirstName() + \" \" + article.getUser().getLastName())", target = "userName")
    @Mapping(source = "user.photo", target = "userPhoto")
    ArticleDTO toDto(Article article);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "coverImage", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Article toEntity(ArticleCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntityFromDto(ArticleCreateDTO dto, @MappingTarget Article article);

    List<ArticleDTO> toDtos(List<Article> articles);
}
