package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.comment.CommentCreateDTO;
import com.desertakal.desertakal.model.dto.comment.CommentDTO;
import com.desertakal.desertakal.model.dto.comment.CommentUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.Comment;
import com.desertakal.desertakal.model.mapper.CommentMapper;
import com.desertakal.desertakal.repository.ArticleRepository;
import com.desertakal.desertakal.repository.CommentRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.service.interfaces.CommentService;
import com.desertakal.desertakal.service.interfaces.NotificationService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository repository;
    private final CommentMapper mapper;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public CommentDTO create(@NonNull CommentCreateDTO dto,@NonNull UUID authorUuid) {

        log.info("Creating comment on article: {} by user: {}",
                dto.getArticleUuid(), authorUuid);

        Article article = findArticle(dto.getArticleUuid());
        User author = findUser(authorUuid);

        Comment comment = mapper.toEntity(dto);
        comment.setArticle(article);
        comment.setUser(author);

        Comment savedComment = repository.save(comment);

        article.incrementCommentCount();
        articleRepository.save(article);

        sendNotificationToArticleOwner(article, author);

        log.info("Comment created: {} on article: {} (new count: {})",
                savedComment.getUuid(), dto.getArticleUuid(), article.getCommentCount());

        return mapper.toDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDTO update(@NonNull UUID commentUuid, @NonNull CommentUpdateDTO dto, @NonNull UUID currentUserUuid) {

        log.info("Updating comment: {} by user: {}",
                commentUuid, currentUserUuid);

        Comment comment = findComment(commentUuid);
        validateOwnership(comment, currentUserUuid);

        mapper.updateEntityFromDto(dto, comment);

        log.info("Comment updated: {}", commentUuid);

        return mapper.toDto(comment);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID commentUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {

        log.info("Deleting comment: {} by user: {} (admin: {})",
                commentUuid, currentUserUuid, isAdmin);

        Comment comment = findComment(commentUuid);

        if (!isAdmin) {
            boolean isCommentOwner = comment.getUser().getUuid().equals(currentUserUuid);
            boolean isArticleAuthor = comment.getArticle().getUser().getUuid().equals(currentUserUuid);

            if (!isCommentOwner && !isArticleAuthor) {
                log.warn("Unauthorized delete attempt on comment {} by user {}",
                        commentUuid, currentUserUuid);
                throw new UnauthorizedActionException("Only the comment author, article author, or an admin can delete this comment.");
            }
        }

        Article article = comment.getArticle();
        article.decrementCommentCount();
        articleRepository.save(article);

        repository.delete(comment);

        log.info("Comment deleted: {} (article count: {})",
                commentUuid, article.getCommentCount());
    }

    @Override
    public CommentDTO getByUuid(@NonNull UUID commentUuid) {
        log.info("Fetching comment: {}", commentUuid);
        Comment comment = findComment(commentUuid);
        return mapper.toDto(comment);
    }

    @Override
    public PaginationDTO getByArticle(@NonNull UUID articleUuid, Pageable pageable) {

        log.info("Fetching comments for article: {} [Page: {}, Size: {}]",
                articleUuid, pageable.getPageNumber(), pageable.getPageSize());

        if (!articleRepository.existsByUuid(articleUuid)) {
            throw new ResourceNotFoundException("Article", "identifier", articleUuid.toString());
        }

        Specification<@NonNull Comment> spec = getSpecification(null, articleUuid);

        Page<@NonNull Comment> commentPage = repository.findAll(spec, pageable);

        log.info("Found {} comments for article {}",
                commentPage.getTotalElements(), articleUuid);

        return buildPaginationDTO(commentPage);
    }


    @Override
    public PaginationDTO getByUser(UUID userUuid, @NonNull Pageable pageable) {
        log.info("Fetching comments by user article: {} [Page: {}, Size: {}]",
                userUuid, pageable.getPageNumber(), pageable.getPageSize());

        if (!userRepository.existsByUuid(userUuid)) {
            throw new ResourceNotFoundException("Article", "identifier", userUuid.toString());
        }

        Specification<@NonNull Comment> spec = getSpecification(userUuid, null);

        Page<@NonNull Comment> commentPage = repository.findAll(spec, pageable);

        log.info("Found {} comments by user {}",
                commentPage.getTotalElements(), userUuid);

        return buildPaginationDTO(commentPage);
    }

    private void sendNotificationToArticleOwner(Article article, User commentAuthor) {
        User articleOwner = article.getUser();

        if (!articleOwner.getUuid().equals(commentAuthor.getUuid())) {
            String title = "New Comment on your article";
            String articlePreview = article.getContent().length() > 30
                    ? article.getContent().substring(0, 30) + "..."
                    : article.getContent();

            String message = String.format("%s commented on your article: '%s'",
                    commentAuthor.getFullName(), articlePreview);

            try {
                notificationService.create(title, message, articleOwner.getUuid());
                log.info("Notification sent to article owner: {}", articleOwner.getUuid());
            } catch (Exception e) {
                log.error("Failed to send notification to article owner: {}", e.getMessage());
            }
        }
    }

    private Comment findComment(@NonNull UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Comment {} not found", uuid);
                    return new ResourceNotFoundException("Comment", "identifier", uuid.toString());
                });
    }

    private Article findArticle(@NonNull UUID uuid) {
        return articleRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Article {} not found", uuid);
                    return new ResourceNotFoundException(
                            "Article", "identifier",
                            uuid.toString());
                });
    }

    private User findUser(@NonNull UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("User {} not found", uuid);
                    return new ResourceNotFoundException("User", "identifier", uuid.toString());
                });
    }

    private void validateOwnership(@NonNull Comment comment, @NonNull UUID currentUserUuid) {
        if (!comment.getUser().getUuid().equals(currentUserUuid)) {
            log.warn("User {} attempted to modify comment {} owned by {}",
                    currentUserUuid, comment.getUuid(), comment.getUser().getUuid());
            throw new UnauthorizedActionException("You are not the author of this comment.");
        }
    }

    private PaginationDTO buildPaginationDTO(Page<@NonNull Comment> page) {
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

    private Specification<@NonNull Comment> getSpecification(UUID userUuid, UUID articleUuid) {
        return (root, query, cb) -> {

            log.debug("Building Review Specification [userUuid: {}, articleUuid: {}]",
                    userUuid, articleUuid);

            List<Predicate> predicates = new ArrayList<>();

            if (userUuid != null) {
                predicates.add(cb.equal(root.get("user").get("uuid"), userUuid));
                log.debug("Filter applied: user.uuid = '{}'", userUuid);
            }

            if (articleUuid != null) {
                predicates.add(cb.equal(root.get("article").get("uuid"), articleUuid));
                log.debug("Filter applied: articleUuid = '{}'", articleUuid);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
