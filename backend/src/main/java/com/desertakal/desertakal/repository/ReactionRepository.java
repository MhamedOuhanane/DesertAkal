package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.Reaction;
import com.desertakal.desertakal.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<@NonNull Reaction, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Reaction> {
    Optional<Reaction> findByUuid(UUID uuid);

    Optional<Reaction> findByUserAndArticle(@NonNull User user, @NonNull Article article);

    long countByArticle(@NonNull Article article);

    @Query("""
        select r.reaction, count(r) from Reaction r
            where r.article = :article
                group by r.reaction
    """)
    List<Object[]> countByArticleUuidGroupByType(@Param("article") Article article);

    boolean existsByUserAndArticle(@NonNull User user, @NonNull Article article);
}
