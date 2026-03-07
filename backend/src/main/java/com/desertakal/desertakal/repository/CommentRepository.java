package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.Comment;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<@NonNull Comment, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Comment> {
    Optional<@NonNull Comment> findByUuid(@NonNull UUID uuid);

    long countByArticle(@NonNull Article article);
    boolean existsByUuid(@NonNull UUID uuid);

}
