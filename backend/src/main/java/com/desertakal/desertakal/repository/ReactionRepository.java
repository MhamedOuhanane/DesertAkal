package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.Reaction;
import com.desertakal.desertakal.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<Reaction> findByUser_UuidAndArticle_Uuid(@NonNull UUID userUuid, @NonNull UUID articleUuid);

    long countByArticle_Uuid(@NonNull UUID articleUuid);



    @Query("""
        select r.reaction, count(r) from Reaction r
            where r.article.uuid = :articleUuid
                group by r.reaction
    """)
    List<Object[]> countByArticleUuidGroupByType(@Param("articleUuid") UUID articleUuid);

    Page<@NonNull Reaction> findByArticle_Uuid(@NonNull UUID articleUuid, @NonNull Pageable pageable);

    boolean existsByUserAndArticle(@NonNull User user, @NonNull Article article);
}
