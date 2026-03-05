package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.exception.custom.ResourceNotFoundException;
import com.desertakal.desertakal.exception.custom.UnauthorizedActionException;
import com.desertakal.desertakal.model.dto.article.ArticleCreateDTO;
import com.desertakal.desertakal.model.dto.article.ArticleDTO;
import com.desertakal.desertakal.model.dto.article.ArticleUpdateDTO;
import com.desertakal.desertakal.model.dto.responce.PaginationDTO;
import com.desertakal.desertakal.model.entity.Article;
import com.desertakal.desertakal.model.entity.User;
import com.desertakal.desertakal.model.mapper.ArticleMapper;
import com.desertakal.desertakal.repository.ArticleRepository;
import com.desertakal.desertakal.repository.UserRepository;
import com.desertakal.desertakal.service.interfaces.ArticleService;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository repository;
    private final ArticleMapper mapper;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ArticleDTO create(@NonNull ArticleCreateDTO dto, @NonNull MultipartFile coverImage, @NonNull UUID authorUuid) {
        log.info("Starting creation of Article by user: {}", authorUuid);
        User author = findUser(authorUuid);

        Article article = mapper.toEntity(dto);
        article.setUser(author);
        article.setCommentCount(0);
        article.setReactionCount(0);

        String imagePath = fileStorageService.uploadDocument(coverImage, "articles");
        article.setCoverImage(imagePath);
        log.info("Cover image uploaded to path: {}", imagePath);

        Article savedArticle = repository.save(article);

        log.info("Article successfully created. UUID: {} by user: {}",
                savedArticle.getUuid(), authorUuid);

        return mapper.toDto(savedArticle);
    }

    @Override
    @Transactional
    public ArticleDTO update(@NonNull UUID articleUuid, ArticleUpdateDTO dto, MultipartFile coverImage, @NonNull UUID currentUserUuid) {

        log.info("Starting update for Article UUID: {} by user: {}",
                articleUuid, currentUserUuid);

        Article article = findArticleWithUser(articleUuid);
        validateOwnership(article, currentUserUuid);

        log.debug("Mapping UpdateDTO to Article entity");
        if (dto != null) {
            mapper.updateEntityFromDto(dto, article);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            replaceImage(article, coverImage);
        }

        Article updatedArticle = repository.save(article);

        log.info("Article [UUID: {}] successfully updated", articleUuid);

        return mapper.toDto(updatedArticle);
    }

    @Override
    @Transactional
    public void delete(@NonNull UUID articleUuid, @NonNull UUID currentUserUuid, boolean isAdmin) {

        log.info("Starting deletion for Article UUID: {} by user: {} (admin: {})",
                articleUuid, currentUserUuid, isAdmin);

        Article article = findArticleWithUser(articleUuid);

        if (!isAdmin) {
            validateOwnership(article, currentUserUuid);
        }

        log.warn("Article identified for deletion - UUID: {}, CreatedAt: {}",
                article.getUuid(), article.getCreatedAt());

        String imagePath = article.getCoverImage();

        repository.delete(article);

        deleteImageSafely(imagePath);

        log.info("Article with UUID: {} successfully deleted", articleUuid);
    }

    @Override
    public PaginationDTO getAll(String owner, @NonNull Pageable pageable) {
        log.info("Fetching all articles [Page: {}, Size: {}]",
                pageable.getPageNumber(), pageable.getPageSize());

        Specification<@NonNull Article> spec = getSpecification(null, owner);

        Page<@NonNull Article> articlePage = repository.findAll(spec, pageable);

        log.info("Found {} articles on page {} of {}",
                articlePage.getNumberOfElements(), articlePage.getNumber(), articlePage.getTotalPages());

        return buildPaginationDTO(articlePage);
    }

    @Override
    public PaginationDTO getByUser(@NonNull UUID userUuid, @NonNull Pageable pageable) {
        log.info("Fetching articles for user: {} [Page: {}, Size: {}]",
                userUuid, pageable.getPageNumber(), pageable.getPageSize());

        if (!userRepository.existsByUuid(userUuid)) {
            log.error("Fetch failed: User with UUID {} not found", userUuid);
            throw new ResourceNotFoundException("User", "identifier", userUuid.toString());
        }

        Specification<@NonNull Article> spec = getSpecification(userUuid, null);

        Page<@NonNull Article> articlePage = repository.findAll(spec, pageable);

        log.info("Found {} articles for user {} on page {} of {}",
                articlePage.getTotalElements(), userUuid,
                articlePage.getNumber(), articlePage.getTotalPages());

        return buildPaginationDTO(articlePage);
    }

    private Article findArticleWithUser(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Article fetch failed: No article found with UUID: {}", uuid);
                    return new ResourceNotFoundException("Article", "uuid", uuid.toString());
                });
    }

    private User findUser(UUID uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("User lookup failed: UUID {} not found in the system", uuid);
                    return new ResourceNotFoundException("User", "uuid", uuid.toString());
                });
    }

    private void validateOwnership(Article article, UUID currentUserUuid) {
        log.debug("Validating ownership for Article: {} against User: {}", article.getUuid(), currentUserUuid);

        if (!article.getUser().getUuid().equals(currentUserUuid)) {
            log.warn("Unauthorized access attempt: User {} tried to modify article {} owned by {}",
                    currentUserUuid, article.getUuid(), article.getUser().getUuid());

            throw new UnauthorizedActionException("You are not the author of this article.");
        }
    }

    private PaginationDTO buildPaginationDTO(Page<@NonNull Article> page) {
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

    private Specification<@NonNull Article> getSpecification(UUID userUuid, String owner) {
        return (root, query, cb) -> {

            log.debug("Building Article Specification [userUuid: {}, owner: {}]",
                    userUuid, owner);

            List<Predicate> predicates = new ArrayList<>();

            if (userUuid != null) {
                predicates.add(cb.equal(root.get("user").get("uuid"), userUuid));
                log.debug("Filter applied: user.uuid = '{}'", userUuid);
            }

            if (owner != null && !owner.isBlank()) {
                String pattern = "%" + owner.toLowerCase() + "%";
                Expression<String> userFullName = cb.concat(cb.concat(root.get("user").get("firstName"), " "), root.get("user").get("lastName"));
                predicates.add(cb.like(cb.lower(userFullName), pattern));
                log.debug("Applying filter: searching for articles by owner name containing: '{}'", owner);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void replaceImage(Article article, MultipartFile newImage) {

        String newImagePath = fileStorageService.uploadDocument(newImage, "articles");
        log.info("New cover image uploaded to: {}", newImagePath);

        String oldImagePath = article.getCoverImage();
        if (oldImagePath != null && !oldImagePath.isBlank() && !oldImagePath.contains("defaults/")) {
            fileStorageService.deleteFile(oldImagePath);
            log.info("Old cover image deleted: {}", oldImagePath);
        }

        article.setCoverImage(newImagePath);
    }

    private void deleteImageSafely(String path) {
        if (path == null || path.isBlank()) return;

        if (path.contains("defaults/")) {
            log.debug("Skipping deletion of default image: {}",path);
            return;
        }

        try {
            fileStorageService.deleteFile(path);
            log.info("Image deleted from MinIO: {}", path);
        } catch (Exception e) {
            log.warn("Failed to delete image '{}': {}",
                    path, e.getMessage());
        }
    }
}
