package com.desertakal.desertakal.repository;

import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<@NonNull Article, @NonNull UUID>, JpaSpecificationExecutor<@NonNull Article> {
    Optional<@NonNull Article> findByUuid(@NonNull UUID uuid);

    long countAllByUser(@NonNull User user);

    boolean existsByUuid(@NonNull UUID uuid);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Article a set a.commentCount =
            (select count(c) from Comment c
                where c.article = a)
                    where a.uuid = :uuid
    """)
    void syncCommentCount(@Param("uuid") UUID uuid);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Article a set a.reactionCount =
            (select count(r) from Reaction r
                where r.article = a)
                    where a.uuid = :uuid
    """)
    void syncReactionCount(@Param("uuid") UUID uuid);
}
