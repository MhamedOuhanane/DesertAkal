package com.desertakal.desertakal.service.interfaces;

import com.desertakal.desertakal.model.dto.article.ArticleCreateDTO;
import com.desertakal.desertakal.model.dto.article.ArticleDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ArticleService {
    ArticleDTO create(@NonNull ArticleCreateDTO dto, @NonNull MultipartFile coverImage, @NonNull UUID authorUuid);

    ArticleDTO update(@NonNull UUID articleUuid, @NonNull ArticleCreateDTO dto, @NonNull MultipartFile coverImage, @NonNull UUID currentUserUuid);

    void delete(@NonNull UUID articleUuid, @NonNull UUID currentUserUuid, boolean isAdmin);

    ArticleDTO getByUuid(UUID articleUuid);

    Page<@NonNull ArticleDTO> getAll(String content, String owner, @NonNull Pageable pageable);

    Page<@NonNull ArticleDTO> getByUser(UUID userUuid, @NonNull Pageable pageable);
}
