package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.model.dto.reaction.ReactionCreateDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionSummaryDTO;
import com.desertakal.desertakal.model.dto.reaction.ReactionToggleResponseDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.Reaction;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.enums.ReactionEnum;
import com.desertakal.desertakal.model.mapper.ReactionMapper;
import com.desertakal.desertakal.repository.ArticleRepository;
import com.desertakal.desertakal.repository.ReactionRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository repository;
    private final ReactionMapper mapper;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public ReactionToggleResponseDTO toggle(@NonNull ReactionCreateDTO dto, @NonNull UUID userUuid) {

        log.info("Toggling reaction {} on article: {} by user: {}",
                dto.getReaction(), dto.getArticleUuid(), userUuid);

        Article article = findArticle(dto.getArticleUuid());
        User user = findUser(userUuid);

        Optional<Reaction> existingOpt = repository.findByUserAndArticle(user, article);

        String action;
        ReactionEnum userReaction;

        if (existingOpt.isPresent()) {
            Reaction existing = existingOpt.get();

            if (existing.getReaction() == dto.getReaction()) {
                repository.delete(existing);
                article.decrementReactionCount();
                articleRepository.save(article);

                action = "REMOVED";
                userReaction = null;

                log.info("Reaction REMOVED: {} from article {} (count: {})",
                        dto.getReaction(), dto.getArticleUuid(),
                        article.getReactionCount());

            } else {
                existing.setReaction(dto.getReaction());
                repository.save(existing);

                action = "CHANGED";
                userReaction = dto.getReaction();

                log.info("Reaction CHANGED to {} on article {}",
                        dto.getReaction(), dto.getArticleUuid());
            }

        } else {
            Reaction newReaction = mapper.toEntity(dto);
            newReaction.setUser(user);
            newReaction.setArticle(article);
            repository.save(newReaction);

            article.incrementReactionCount();
            articleRepository.save(article);

            action = "ADDED";
            userReaction = dto.getReaction();

            log.info("Reaction ADDED: {} on article {} (count: {})",
                    dto.getReaction(), dto.getArticleUuid(),
                    article.getReactionCount());
        }

        Map<ReactionEnum, Long> countByType = buildCountByType(dto.getArticleUuid());

        return ReactionToggleResponseDTO.builder()
                .action(action)
                .userReaction(userReaction)
                .totalCount(article.getReactionCount())
                .countByType(countByType)
                .articleUuid(dto.getArticleUuid())
                .build();
    }

    @Override
    public ReactionSummaryDTO getSummary(@NonNull UUID articleUuid, @NonNull UUID currentUserUuid) {
        log.info("Request received for reaction summary | Article: {} | User: {}", articleUuid, currentUserUuid);

        if (!articleRepository.existsByUuid(articleUuid)) {
            log.warn("Summary fetch failed: Article {} does not exist", articleUuid);
            throw new ResourceNotFoundException("Article", "identifier", articleUuid.toString());
        }

        long totalCount = repository.countByArticle_Uuid(articleUuid);
        log.debug("Total reaction count for article {}: {}", articleUuid, totalCount);

        Map<ReactionEnum, Long> countByType = buildCountByType(articleUuid);
        log.debug("Distribution by type for article {}: {}", articleUuid, countByType);

        ReactionEnum userReaction = repository.findByUser_UuidAndArticle_Uuid(currentUserUuid, articleUuid)
                .map(reaction -> {
                    log.debug("Found existing reaction '{}' for user {} on article {}",
                            reaction.getReaction(), currentUserUuid, articleUuid);
                    return reaction.getReaction();
                })
                .orElseGet(() -> {
                    log.debug("No reaction found for user {} on article {}", currentUserUuid, articleUuid);
                    return null;
                });

        log.info("Reaction summary successfully built for article: {}", articleUuid);

        return ReactionSummaryDTO.builder()
                .totalCount(totalCount)
                .countByType(countByType)
                .userReaction(userReaction)
                .articleUuid(articleUuid)
                .build();
    }

    @Override
    public PaginationDTO getByArticle(@NonNull UUID articleUuid, ReactionEnum type, @NonNull Pageable pageable) {
        log.info("Fetching reaction list | Article: {} | Page: {} | Size: {}",
                articleUuid, pageable.getPageNumber(), pageable.getPageSize());

        if (!articleRepository.existsByUuid(articleUuid)) {
            log.error("Failed to list reactions: Article {} not found", articleUuid);
            throw new ResourceNotFoundException("Article", "identifier", articleUuid.toString());
        }

        Page<@NonNull Reaction> reactionPage;
        if (type != null) {
            reactionPage = repository.findByArticle_UuidAndReaction(articleUuid, type, pageable);
        } else {
            reactionPage = repository.findByArticle_Uuid(articleUuid, pageable);
        }

        log.info("Reactions retrieved successfully | Total elements: {} | Total pages: {}",
                reactionPage.getTotalElements(), reactionPage.getTotalPages());

        return buildPaginationDTO(reactionPage);
    }

    private Article findArticle(UUID uuid) {
        return articleRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Article {} not found", uuid);
                    return new ResourceNotFoundException("Article", "identifier", uuid.toString());
                });
    }

    private User findUser(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("User {} not found", uuid);
                    return new ResourceNotFoundException("User", "identifier", uuid.toString());
                });
    }

    private Map<ReactionEnum, Long> buildCountByType(UUID articleUuid) {

        List<Object[]> rawCounts = repository.countByArticleUuidGroupByType(articleUuid);

        Map<ReactionEnum, Long> countByType = new LinkedHashMap<>();

        for (Object[] row : rawCounts) {
            ReactionEnum type = (ReactionEnum) row[0];
            Long count = (Long) row[1];
            countByType.put(type, count);
        }

        return countByType;
    }

    private PaginationDTO buildPaginationDTO(Page<@NonNull Reaction> page) {
        return PaginationDTO.builder()
                .content(mapper.toDtos(page.getContent()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
